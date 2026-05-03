package com.quickthought.skillvault.ui.addedit

import com.quickthought.skillvault.domain.model.CredentialItemUI

class AddEditContract {

    // State to hold the current values of the text fields
    data class UiState(
        val credentialId: Int? = null,
        val title: String = "Add New Credential",
        val accountName: String = "",
        val username: String = "",
        val password: String = "",
        val isSaving: Boolean = false,
        val isEditMode: Boolean = false,
        val showDeleteConfirmation: Boolean = false,
        val showOverwriteConfirmation: Boolean = false
    )

    sealed class UiEvent {
        object SaveSuccess : UiEvent()
        object DeleteSuccess : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }

    sealed class ViewAction {
        data class Initialize(val credential: CredentialItemUI?) : ViewAction()
        // To clear the state when the FAB is pressed
        object ResetState : ViewAction()
        data class AccountNameChanged(val name: String) : ViewAction()
        data class UsernameChanged(val name: String) : ViewAction()
        data class GeneratePassword(val length: Int = 16) : ViewAction()
        data class PasswordChanged(val password: String) : ViewAction()
        object SaveTapped : ViewAction()
        object DeleteTapped : ViewAction()
        object DeleteCancelled : ViewAction()
        object DeleteConfirmed : ViewAction()
        object ConfirmOverwrite : ViewAction()
        object CancelOverwrite : ViewAction()
    }
}