package com.quickthought.skillvault.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditCredentialSheet
import com.quickthought.skillvault.ui.list.CredentialListContract.UiEvent
import com.quickthought.skillvault.ui.list.CredentialListContract.UiState
import com.quickthought.skillvault.ui.list.CredentialListContract.ViewAction
import com.quickthought.skillvault.ui.list.components.BiometricAuthenticator
import com.quickthought.skillvault.ui.list.components.CredentialItem
import com.quickthought.skillvault.ui.list.components.EmptyState
import com.quickthought.skillvault.ui.list.components.LoadingState
import com.quickthought.skillvault.ui.list.components.copyTextToClipboard
import com.quickthought.skillvault.ui.widgets.DeleteConfirmationDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialListScreen(
    viewModel: CredentialListViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    // Holds the Credential object to be edited (null if adding a new one)
    val credentialToEdit = remember { mutableStateOf<CredentialItemUI?>(null) }

    // State to control the visibility of the Add/Edit Bottom Sheet
    val showSheet = remember { mutableStateOf(false) }

    // haptic feedback
    val sendHaptic = remember { mutableStateOf(false) }

    // --- Biometric Authentication Handling ---
    val biometricAuthenticator = remember { BiometricAuthenticator(context) }

    val searchQuery by viewModel.searchQuery.collectAsState()

    // Collect one-time events (Snackbar, Biometric Prompt)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is UiEvent.CopyToClipBoard -> {
                    copyTextToClipboard(context, event.password)
                    sendHaptic.value = true
                }

                UiEvent.ShowBiometricPrompt -> {
                    biometricAuthenticator.prompt(
                        title = "Verify Identity",
                        subtitle = "Authenticate to reveal password",
                        onSuccess = { viewModel.handleAuthenticationSuccess() },
                        onFailure = {
                            // Show a snackbar on failure
                            viewModel.showErrorMessage("Authentication failed.")
                        }
                    )
                }

                UiEvent.OpenAddSheet -> {
                    credentialToEdit.value = null
                    showSheet.value = true
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(title = { Text("SkillVault") })

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.processAction(ViewAction.SearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search accounts...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.processAction(
                                    ViewAction.SearchQueryChanged(
                                        ""
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                credentialToEdit.value = null
                showSheet.value = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Credential")
            }
        }
    ) { paddingValues ->

        if (sendHaptic.value) {
            val haptic = LocalHapticFeedback.current
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            LaunchedEffect(Unit) {
                delay(1000)
                sendHaptic.value = false
            }
        }

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (state) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> ErrorState((state as UiState.Error).message)
                is UiState.Success -> CredentialListContent(
                    credentials = (state as UiState.Success).credentials,
                    onItemClick = {
                        showSheet.value = true
                        credentialToEdit.value = it
                        viewModel.processAction(ViewAction.CredentialTapped(it))
                    },
                    onCopyClick = { viewModel.processAction(ViewAction.CopyPasswordClicked(it.credentialId)) },
                    onDeleteClick = { viewModel.processAction(ViewAction.DeleteIconClicked(it)) },
                    onConfirmDeleteClick = { viewModel.processAction(ViewAction.ConfirmDelete) },
                    onDismissDeleteClick = { viewModel.processAction(ViewAction.DismissDeleteDialog) },
                    pendingDeleteId = (state as UiState.Success).pendingDeleteId
                )
            }
        }
    }

    // Render the Add/Edit Bottom Sheet
    if (showSheet.value) {
        AddEditCredentialSheet(
            onDismiss = {
                showSheet.value = false
                credentialToEdit.value = null // Clear the model
            },
            initialCredential = credentialToEdit.value
        )
    }
}

@Composable
fun CredentialListContent(
    credentials: List<CredentialItemUI>,
    onItemClick: (CredentialItemUI) -> Unit,
    onCopyClick: (CredentialItemUI) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onConfirmDeleteClick: () -> Unit,
    onDismissDeleteClick: () -> Unit,
    pendingDeleteId: Int? = null
) {
    if (credentials.isEmpty()) {
        EmptyState("No credentials saved yet.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(credentials, key = { it.credentialId }) { credential ->
                CredentialItem(
                    credential = credential,
                    onClick = { onItemClick(credential) },
                    onCopyClick = { onCopyClick(credential) },
                    onDeleteClick = { onDeleteClick(credential.credentialId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (pendingDeleteId != null) {
            // Show the delete confirmation dialog
            DeleteConfirmationDialog(
                onConfirm = { onConfirmDeleteClick() },
                onDismiss = { onDismissDeleteClick() }
            )
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Text(
        text = "Error: $message",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CredentialListContentPreview() {
    val dummyCredentials = listOf(
        CredentialItemUI(1, "Google", "john.doe@gmail.com"),
        CredentialItemUI(2, "Facebook", "john.d"),
        CredentialItemUI(3, "Twitter", "@john_doe")
    )
    CredentialListContent(
        credentials = dummyCredentials,
        onItemClick = {},
        onCopyClick = {},
        onDeleteClick = {},
        onConfirmDeleteClick = {},
        onDismissDeleteClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CredentialListContentEmptyPreview() {
    CredentialListContent(
        credentials = emptyList(),
        onItemClick = {},
        onCopyClick = {},
        onDeleteClick = {},
        onConfirmDeleteClick = {},
        onDismissDeleteClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    ErrorState(message = "Something went wrong while loading data.")
}