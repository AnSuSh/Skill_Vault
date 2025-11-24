package com.quickthought.skillvault.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiEvent
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiState
import com.quickthought.skillvault.ui.addedit.AddEditContract.ViewAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: CredentialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    fun processAction(action: ViewAction) {
        when (action) {
            is ViewAction.Initialize -> initialize(action.credential)
            is ViewAction.AccountNameChanged -> _uiState.update { it.copy(accountName = action.name) }
            is ViewAction.UsernameChanged -> _uiState.update { it.copy(username = action.name) }
            is ViewAction.PasswordChanged -> _uiState.update { it.copy(password = action.password) }
            ViewAction.SaveTapped -> saveCredential()
            ViewAction.DeleteTapped -> _uiState.update { it.copy(showDeleteConfirmation = true) }
            ViewAction.ConfirmDelete -> deleteCredential()
        }
    }

    private fun initialize(credential: CredentialItemUI?) {
        credential?.let {
            _uiState.value = UiState(
                credentialId = it.credentialId,
                title = "Edit Credential",
                accountName = it.accountName,
                username = it.username,
                isEditMode = true
                // Password is intentionally left blank for security reasons
            )
        }
    }

    private fun saveCredential() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            if (state.accountName.isBlank() || state.username.isBlank() || (state.password.isBlank() && !state.isEditMode)) {
                _uiEvent.emit(UiEvent.ShowError("Please fill in account name, username, and password."))
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }

            try {
                // Create a model with current state (ID is 0 if adding)
                val credentialToSave = CredentialItemUI(
                    credentialId = state.credentialId ?: 0,
                    accountName = state.accountName,
                    username = state.username
                )

                // Pass the domain model and the plaintext password to the Repository
                repository.saveCredential(credentialToSave, state.password)
                _uiEvent.emit(UiEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to save: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun deleteCredential() {
        val id = _uiState.value.credentialId
        if (id == null) return

        viewModelScope.launch {
            try {
                repository.deleteCredential(id)
                _uiEvent.emit(UiEvent.DeleteSuccess)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to delete: ${e.message}"))
            }
        }
    }
}