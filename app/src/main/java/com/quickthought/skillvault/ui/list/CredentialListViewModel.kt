package com.quickthought.skillvault.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.ui.list.CredentialListContract.UiEvent
import com.quickthought.skillvault.ui.list.CredentialListContract.UiState
import com.quickthought.skillvault.ui.list.CredentialListContract.ViewAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredentialListViewModel @Inject constructor(
    private val credentialRepository: CredentialRepository
) : ViewModel() {

    // --- State Management ---
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    // SharedFlow is used for one-time events (like showing a snackbar)
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    // --- Data Caching for Actions ---
    // Stores the ID of the credential we are currently trying to copy/view
    private var pendingCredentialId: Int? = null

    init {
        // Start loading data immediately when the ViewModel is created
        processAction(ViewAction.LoadCredentials)
    }

    fun processAction(action: ViewAction) {
        when (action) {
            ViewAction.LoadCredentials -> loadCredentials()
            is ViewAction.CopyPasswordClicked -> handleCopyPasswordClicked(action.credentialId)
            is ViewAction.CredentialTapped -> { /* Logic to open AddEdit sheet */
            }
        }
    }

    private fun loadCredentials() {
        // Collects the Flow from the Repository and updates the UI state reactively
        credentialRepository.getCredentials()
            .onStart { _uiState.value = UiState.Loading }
            .onEach { credentialItems ->
                _uiState.value = UiState.Success(credentialItems)
            }
            .catch { e ->
                _uiState.value = UiState.Error("Failed to load credentials: ${e.message}")
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
    fun handleAuthenticationSuccess() {
        val id = pendingCredentialId ?: return // Should not be null if called correctly

        viewModelScope.launch {
            try {
                // 1. Get the decrypted password from the secure Repository
                val password = credentialRepository.getDecryptedPassword(id)

                // 2. Copy the password to the clipboard (You'll implement this in the UI/Activity)
                // Assuming ClipboardManager is handled outside the ViewModel for Android Context dependency.

                // 3. Notify the user
                _uiEvent.emit(UiEvent.ShowSnackbar("Password copied to clipboard!"))

            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Security Error: ${e.message}"))
            } finally {
                // 4. Clear the pending ID immediately
                pendingCredentialId = null
            }
        }
    }
}