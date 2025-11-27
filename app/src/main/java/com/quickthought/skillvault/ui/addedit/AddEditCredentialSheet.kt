package com.quickthought.skillvault.ui.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiEvent
import com.quickthought.skillvault.ui.addedit.AddEditContract.ViewAction
import com.quickthought.skillvault.ui.widgets.DeleteConfirmationDialog
import com.quickthought.skillvault.util.PasswordGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCredentialSheet(
    onDismiss: () -> Unit,
    initialCredential: CredentialItemUI? = null, // Passed from the list screen for edit mode
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var passwordVisible by remember { mutableStateOf(false) }

    // Initialize the ViewModel with data if in edit mode
    LaunchedEffect(initialCredential) {
        if (initialCredential == null) {
            // If we are opening the sheet for a new item (Add mode), reset the state
            viewModel.processAction(ViewAction.ResetState)
        } else {
            // If we are editing, initialize with data
            viewModel.processAction(ViewAction.Initialize(initialCredential))
        }
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
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text,
                    autoCorrectEnabled = true,
                ),
                onValueChange = { viewModel.processAction(ViewAction.AccountNameChanged(it)) },
                label = { Text("Account Name (e.g., Google, Bank)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.username,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email,
                    autoCorrectEnabled = true,
                ),
                onValueChange = { viewModel.processAction(ViewAction.UsernameChanged(it)) },
                label = { Text("Username / Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.password,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    onValueChange = { viewModel.processAction(ViewAction.PasswordChanged(it)) },
                    label = { Text("Password *") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.weight(9f)
                )
                IconButton(
                    onClick = {
                        viewModel.processAction(ViewAction.GeneratePassword())
                    }, colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Generate Password")
                }
            }
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
                DeleteConfirmationDialog(
                    onConfirm = { viewModel.processAction(ViewAction.ConfirmDelete) },
                    onDismiss = { viewModel.processAction(ViewAction.DeleteCancelled) }
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