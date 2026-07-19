package com.quickthought.skillvault.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickthought.skillvault.R
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiEvent
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiState
import com.quickthought.skillvault.ui.addedit.AddEditContract.ViewAction
import com.quickthought.skillvault.util.PasswordGenerator
import com.quickthought.skillvault.util.VaultLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for adding or editing credentials.
 * Handles the logic for saving, updating, and deleting credentials.
 *
 * @property repository The repository to fetch and manage credentials.
 */
@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: CredentialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    /**
     * Processes actions triggered by the view.
     *
     * @param action The action to be processed.
     */
    fun processAction(action: ViewAction) {
        when (action) {
            is ViewAction.Initialize -> initialize(action.credential)
            is ViewAction.AccountNameChanged -> _uiState.update { it.copy(accountName = action.name) }
            is ViewAction.UsernameChanged -> _uiState.update { it.copy(username = action.name) }
            is ViewAction.PasswordChanged -> _uiState.update { it.copy(password = action.password) }
            is ViewAction.WebsiteUrlChanged -> _uiState.update { it.copy(websiteUrl = action.url) }
            is ViewAction.PackageNameChanged -> _uiState.update { it.copy(packageName = action.packageName) }
            ViewAction.SaveTapped -> processSaveTapped()
            ViewAction.DeleteTapped -> _uiState.update { it.copy(showDeleteConfirmation = true) }
            ViewAction.DeleteConfirmed -> deleteCredential()
            // Reset the entire state to the default empty state
            ViewAction.ResetState -> _uiState.value = UiState()
            ViewAction.DeleteCancelled -> _uiState.update { it.copy(showDeleteConfirmation = false) }
            ViewAction.CancelOverwrite -> cancelOverwrite()
            ViewAction.ConfirmOverwrite -> confirmOverwrite()
        }
    }

    /**
     * Initializes the ViewModel with an existing credential for editing.
     *
     * @param credential The credential to be edited, or null for a new one.
     */
    private fun initialize(credential: CredentialItemUI?) {
        credential?.let {
            _uiState.value = UiState(
                credentialId = it.credentialId,
                title = "Edit Credential",
                accountName = it.accountName,
                username = it.username,
                websiteUrl = it.websiteUrl ?: "",
                packageName = it.packageName ?: "",
                isEditMode = true
                // Password is intentionally left blank for security reasons
            )
        }
    }

    /**
     * Handles the save action triggered by the user.
     * Performs validation and either saves the credential or shows an overwrite confirmation.
     */
    private fun processSaveTapped() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            if (state.accountName.isBlank() || state.username.isBlank() || (state.password.isBlank() && state.credentialId == null)) {
                _uiEvent.emit(UiEvent.ShowError(messageResId = R.string.fill_fields_error))
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }

            if (state.isEditMode) {
                // An item with this name already exists, show confirmation dialog
                _uiState.update { it.copy(showOverwriteConfirmation = true) }
            } else {
                // No existing item, proceed with saving
                saveCredential()
            }
        }
    }

    private fun confirmOverwrite() {
        viewModelScope.launch {
            // Hide dialog and proceed with saving
            _uiState.update { it.copy(showOverwriteConfirmation = false) }
            saveCredential()
        }
    }

    private fun cancelOverwrite() {
        _uiState.update { it.copy(showOverwriteConfirmation = false) }
    }

    /**
     * Proceeds with saving the credential after user confirmation.
     */
    private suspend fun saveCredential() {
        val state = _uiState.value
        // If in Edit Mode and the password field is blank, we must fetch the old encrypted password
        // to avoid overwriting it with an empty string.
        val passwordToEncrypt = if (state.isEditMode && state.password.isBlank()) {
            // Placeholder: In a perfect world, the Repository would handle this complex update logic
            // by fetching the existing entity's encrypted password.
            // For V1 simplicity, we assume the user must re-enter the password to update the item.
            // We will simplify this and just prevent updating if the password field is blank in Edit Mode.
            _uiEvent.emit(UiEvent.ShowError(messageResId = R.string.update_password_error))
            _uiState.update { it.copy(isSaving = false) }
            return
        } else {
            state.password // Use the new password
        }

        try {
            // Create a model with current state (ID is 0 if adding)
            val credentialToSave = CredentialItemUI(
                credentialId = state.credentialId ?: 0,
                accountName = state.accountName,
                username = state.username,
                websiteUrl = state.websiteUrl.ifBlank { null },
                packageName = state.packageName.ifBlank { null }
            )

            VaultLogger.infoLog("Saving credential: $credentialToSave")
            // Pass the domain model and the plaintext password to the Repository
            repository.saveCredential(credentialToSave, passwordToEncrypt)
            _uiEvent.emit(UiEvent.SaveSuccess)
        } catch (e: Exception) {
            VaultLogger.errorLog("Error saving credential: ${e.message}")
            _uiEvent.emit(UiEvent.ShowError(message = e.message ?: "Unknown error"))
        } finally {
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    /**
     * Deletes the current credential from the repository.
     */
    private fun deleteCredential() {
        val id = _uiState.value.credentialId
        if (id == null) return

        viewModelScope.launch {
            try {
                repository.deleteCredential(id)
                _uiEvent.emit(UiEvent.DeleteSuccess)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError(message = e.message ?: "Unknown error"))
            }
        }
    }
}