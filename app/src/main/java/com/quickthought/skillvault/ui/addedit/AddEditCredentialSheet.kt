package com.quickthought.skillvault.ui.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiEvent
import com.quickthought.skillvault.ui.addedit.AddEditContract.ViewAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCredentialSheet(
    onDismiss: () -> Unit,
    initialCredentialId: Int? = null, // Passed from the list screen for edit mode
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize the ViewModel with data if in edit mode
    LaunchedEffect(initialCredentialId) {
        viewModel.processAction(
            ViewAction.Initialize(
                credential = null // This should be handled by retrieving the full model in a proper flow,
                // but for a simple V1, the ViewModel could fetch it if ID is present.
                // For now, assume a complex navigation flow passes the model, or the VM fetches it.
                // For simplicity here, we assume the list screen handles the "edit" flow state.
            )
        )
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UiEvent.SaveSuccess, UiEvent.DeleteSuccess -> onDismiss()
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Sheet Title
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input Fields
            OutlinedTextField(
                value = state.accountName,
                onValueChange = { viewModel.processAction(ViewAction.AccountNameChanged(it)) },
                label = { Text("Account Name (e.g., Google, Bank)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.username,
                onValueChange = { viewModel.processAction(ViewAction.UsernameChanged(it)) },
                label = { Text("Username / Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.processAction(ViewAction.PasswordChanged(it)) },
                label = { Text("Password (Required for New/Change)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (state.isEditMode) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button (only visible in edit mode)
                if (state.isEditMode) {
                    TextButton(onClick = { viewModel.processAction(ViewAction.DeleteTapped) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DELETE", color = MaterialTheme.colorScheme.error)
                    }
                }

                // Save Button
                Button(
                    onClick = { viewModel.processAction(ViewAction.SaveTapped) },
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("SAVE")
                    }
                }
            }

            // Delete Confirmation Dialog
            if (state.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { /* Do nothing on dismiss, force choice */ },
                    confirmButton = {
                        TextButton(onClick = { viewModel.processAction(ViewAction.ConfirmDelete) }) {
                            Text("Confirm Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
//                        TextButton(onClick = {
//                            viewModel.processAction(ViewAction.DeleteTapped) {
//                            Text("Cancel")
//                        }
                    },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete this credential?") }
                )
            }
        }
        SnackbarHost(snackbarHostState)
    }
}

@Preview
@Composable
fun AddEditCredentialSheetPreview() {
    AddEditCredentialSheet(onDismiss = {})
}