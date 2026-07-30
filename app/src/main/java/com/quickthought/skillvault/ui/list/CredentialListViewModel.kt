package com.quickthought.skillvault.ui.list

import android.content.SharedPreferences
import androidx.core.content.edit
import android.view.autofill.AutofillManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickthought.skillvault.R
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.ui.list.CredentialListContract.UiEvent
import com.quickthought.skillvault.ui.list.CredentialListContract.UiEvent.ShowSnackbar
import com.quickthought.skillvault.ui.list.CredentialListContract.UiState
import com.quickthought.skillvault.ui.list.CredentialListContract.ViewAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the [CredentialListScreen].
 * Manages the UI state, search query, and actions for the credential list.
 *
 * @property credentialRepository The repository to fetch and manage credentials.
 */
@HiltViewModel
class CredentialListViewModel @Inject constructor(
    private val credentialRepository: CredentialRepository,
    private val autofillManager: AutofillManager?,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // --- State Management ---
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    // SharedFlow is used for one-time events (like showing a snackbar)
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // --- Data Caching for Actions ---
    // Stores the ID of the credential we are currently trying to copy/view
    private var pendingCredentialId: Int? = null

    init {
        // Start loading data immediately when the ViewModel is created
        processAction(ViewAction.LoadCredentials)
    }

    /**
     * Processes actions triggered by the view.
     *
     * @param action The action to be processed.
     */
    fun processAction(action: ViewAction) {
        when (action) {
            is ViewAction.CopyPasswordClicked -> handleCopyPasswordClicked(action.credentialId)
            is ViewAction.DeleteIconClicked -> handleDeleteIconClicked(action.id)
            is ViewAction.SearchQueryChanged -> onSearchQueryChanged(action.query)
            ViewAction.LoadCredentials -> loadCredentials()
            ViewAction.ConfirmDelete -> handleConfirmDelete()
            ViewAction.DismissDeleteDialog -> handleCancelledDeleteFlow()
            ViewAction.CheckAutofillService -> checkAutofillServiceStatus()
            ViewAction.DismissAutofillPrompt -> handleDismissAutofillPrompt()
            ViewAction.NeverAskAutofillClicked -> handleNeverAskAutofillClicked()
            ViewAction.OpenAutofillSettings -> handleOpenAutofillSettings()
        }
    }

    // Inside CredentialListViewModel
    /**
     * Handles shortcut actions (e.g., from app shortcuts).
     *
     * @param action The shortcut action string.
     */
    fun handleShortcutAction(action: String?) {
        if (action == "add_new") {
            // We trigger the same logic as clicking the FAB
            // If your state-driven sheet logic uses a 'showSheet' boolean,
            // we might need to emit a one-time event.
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.OpenAddSheet)
            }
        }
    }

    private fun handleCancelledDeleteFlow() {
        if (_uiState.value is UiState.Success) {
            _uiState.update {
                (it as UiState.Success).copy(pendingDeleteId = null)
            }
        }
    }

    private fun checkAutofillServiceStatus() {
        val neverAskAgain = sharedPreferences.getBoolean("never_ask_autofill", false)
        if (neverAskAgain) return

        val isEnabled = autofillManager?.hasEnabledAutofillServices() ?: false
        if (isEnabled) {
            // If it's enabled, make sure the prompt is hidden if it was somehow shown
            if (_uiState.value is UiState.Success) {
                _uiState.update { (it as UiState.Success).copy(showAutofillPrompt = false) }
            }
            return
        }

        val lastPromptTime = sharedPreferences.getLong("last_autofill_prompt_time", 0L)
        val currentTime = System.currentTimeMillis()
        val cooldownMillis = 24 * 60 * 60 * 1000L // 24 hours cooldown

        if (currentTime - lastPromptTime < cooldownMillis) return

        if (_uiState.value is UiState.Success) {
            _uiState.update {
                (it as UiState.Success).copy(showAutofillPrompt = true)
            }
            // Update last prompt time when shown
            sharedPreferences.edit { putLong("last_autofill_prompt_time", currentTime) }
        }
    }

    private fun handleDismissAutofillPrompt() {
        if (_uiState.value is UiState.Success) {
            _uiState.update {
                (it as UiState.Success).copy(showAutofillPrompt = false)
            }
        }
    }

    private fun handleNeverAskAutofillClicked() {
        sharedPreferences.edit { putBoolean("never_ask_autofill", true) }
        handleDismissAutofillPrompt()
    }

    private fun handleOpenAutofillSettings() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateToAutofillSettings)
            handleDismissAutofillPrompt()
        }
    }

    private fun handleDeleteIconClicked(id: Int) {
        // We just update the state. The UI will react to 'pendingDeleteId' not being null.
        if (_uiState.value is UiState.Success) {
            _uiState.update {
                (it as UiState.Success).copy(pendingDeleteId = id)
            }
        } else return // Think again in future to find a better solution
    }

    private fun handleConfirmDelete() {
        when (val state = _uiState.value) {
            is UiState.Success -> {
                val idToDelete = state.pendingDeleteId ?: return

                viewModelScope.launch {
                    _uiState.update { state.copy(isDeleting = true, pendingDeleteId = null) }
                    deleteCredential(idToDelete)
                    _uiState.update { state.copy(isDeleting = false) }
                }
            }

            else -> return
        }
    }

    private fun deleteCredential(id: Int) {
        viewModelScope.launch {
            try {
                credentialRepository.deleteCredential(id)
                _uiEvent.emit(ShowSnackbar("Credential deleted successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(ShowSnackbar("Failed to delete: ${e.message}"))
            }
        }
    }

    private    /**
     * Updates the search query and triggers a reload of credentials.
     *
     * @param query The new search query.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        loadCredentials()
    }

    @OptIn(FlowPreview::class)
    /**
     * Loads credentials from the repository, optionally filtering by search query.
     */
    private fun loadCredentials() {
        // Collects the Flow from the Repository and updates the UI state reactively
        credentialRepository.getCredentials()
            .onStart { _uiState.value = UiState.Loading }
            .combine(_searchQuery.debounce(300)) { credentials, query ->
                if (query.isEmpty()) credentials
                else credentials.filter { it.accountName.contains(query, ignoreCase = true) }
            }
            .onEach { filteredCredentialItems ->
                _uiState.value = UiState.Success(filteredCredentialItems)
            }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
            .launchIn(viewModelScope) // Attaches the flow collection to the ViewModel's lifecycle
    }

    private fun handleCopyPasswordClicked(credentialId: Int) {
        // 1. Cache the ID of the item the user wants to copy
        pendingCredentialId = credentialId

        // 2. Trigger a one-time UI event to show the Biometric Prompt
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowBiometricPrompt)
        }
    }

    /**
     * Called by the Activity/Fragment after successful Biometric Authentication.
     */
    /**
     * Handles successful biometric authentication.
     * Reveals the password for the pending credential and copies it to the clipboard.
     */
    fun handleAuthenticationSuccess() {
        val id = pendingCredentialId ?: return // Should not be null if called correctly

        viewModelScope.launch {
            try {
                // 1. Get the decrypted password from the secure Repository
                val password = credentialRepository.getDecryptedPassword(id)

                // 2. Copy the password to the clipboard (You'll implement this in the UI/Activity)
                _uiEvent.emit(UiEvent.CopyToClipBoard(password))

                // 3. Notify the user
                _uiEvent.emit(UiEvent.ShowSnackbar(messageResId = R.string.password_copied))

            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar(message = e.message ?: "Unknown error"))
            } finally {
                // 4. Clear the pending ID immediately
                pendingCredentialId = null
            }
        }
    }

    fun showErrorMessage(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowSnackbar(message = message))
        }
    }
}