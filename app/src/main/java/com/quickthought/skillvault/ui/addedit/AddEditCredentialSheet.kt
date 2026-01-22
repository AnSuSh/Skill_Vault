package com.quickthought.skillvault.ui.addedit

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickthought.skillvault.R
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiEvent
import com.quickthought.skillvault.ui.addedit.AddEditContract.ViewAction
import com.quickthought.skillvault.ui.widgets.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCredentialHost( // This is the new entry point
    onDismiss: () -> Unit,
    initialCredential: CredentialItemUI? = null,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

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

    // This handles closing the sheet/dialog on success
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UiEvent.SaveSuccess, UiEvent.DeleteSuccess -> onDismiss()
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // For Landscape: Use a Dialog
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AddEditContent(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
        }
    } else {
        // For Portrait: Use the ModalBottomSheet
        ModalBottomSheet(onDismissRequest = onDismiss) {
            AddEditContent(viewModel = viewModel, snackbarHostState = snackbarHostState)
        }
    }
}

@Composable
fun AddEditContent(
    viewModel: AddEditViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    // This Column now contains all your UI elements (Text, TextFields, Buttons)
    // It's designed to be placed inside any container (sheet, dialog, etc.)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .navigationBarsPadding()
    ) {
        // Sheet Title
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp) // Add top padding
        )

        // Input Fields (your existing OutlinedTextFields and Row)
        OutlinedTextField(
            value = state.accountName,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text,
                autoCorrectEnabled = true,
            ),
            onValueChange = { viewModel.processAction(ViewAction.AccountNameChanged(it)) },
            label = { Text(stringResource(R.string.account_name_hint)) },
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
            label = { Text(stringResource(R.string.username_email_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.password,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                ),
                onValueChange = { viewModel.processAction(ViewAction.PasswordChanged(it)) },
                label = { Text(stringResource(R.string.password_hint)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image =
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) stringResource(R.string.hide_password_desc) else stringResource(
                        R.string.show_password_desc
                    )

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.weight(9f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.processAction(ViewAction.GeneratePassword())
                }, colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.generate_password_desc))
            }
        }

        // Action Buttons (your existing Row with Delete and Save buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (state.isEditMode) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isEditMode) {
                TextButton(onClick = { viewModel.processAction(ViewAction.DeleteTapped) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_text))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DELETE", color = MaterialTheme.colorScheme.error)
                }
            }
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

        Spacer(modifier = Modifier.height(24.dp))

        // Delete Confirmation Dialog
        if (state.showDeleteConfirmation) {
            ConfirmationDialog(
                title = stringResource(R.string.delete_password_alert),
                text = stringResource(R.string.irreversible_action_warning),
                onConfirm = { viewModel.processAction(ViewAction.DeleteConfirmed) },
                onDismiss = { viewModel.processAction(ViewAction.DeleteCancelled) }
            )
        }

        if (state.showOverwriteConfirmation){
            ConfirmationDialog(
                title = stringResource(R.string.save_password_alert),
                text = stringResource(R.string.save_password_warning),
                onConfirm = { viewModel.processAction(ViewAction.ConfirmOverwrite) },
                onDismiss = { viewModel.processAction(ViewAction.CancelOverwrite) }
            )
        }

        // SnackbarHost is important for showing errors
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}