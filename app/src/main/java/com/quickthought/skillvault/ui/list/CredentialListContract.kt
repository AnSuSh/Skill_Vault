package com.quickthought.skillvault.ui.list

import com.quickthought.skillvault.domain.model.CredentialItemUI

/**
 * Defines the contract (State and Events) for the Credential List screen.
 */
class CredentialListContract {

    /**
     * Represents the immutable state of the UI at any given time.
     */
    sealed class UiState {
        // Data class holding the success state and list of credentials
        data class Success(
            val credentials: List<CredentialItemUI>,
            val pendingDeleteId: Int? = null, // If not null, the dialog is visible
            val isDeleting: Boolean = false
        ) : UiState()

        // Object representing the initial loading state
        object Loading : UiState()

        // Data class for any error that needs to be displayed
        data class Error(val message: String) : UiState()
    }

    /**
     * Represents a one-time event that triggers a UI action (e.g., showing a Snackbar).
     */
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class CopyToClipBoard(val password: String) : UiEvent()
        object ShowBiometricPrompt : UiEvent()
        object OpenAddSheet : UiEvent()
//        object NavigateToSettings : UiEvent()
    }

    /**
     * Defines all possible inputs (Actions/Intents) from the UI to the ViewModel.
     */
    sealed class ViewAction {
        object LoadCredentials : ViewAction()
        data class CopyPasswordClicked(val credentialId: Int) : ViewAction()
        data class CredentialTapped(val credential: CredentialItemUI) : ViewAction()
        data class SearchQueryChanged(val query: String) : ViewAction()
        data class DeleteIconClicked(val id: Int) : ViewAction()
        object ConfirmDelete : ViewAction()
        object DismissDeleteDialog : ViewAction()
    }
}