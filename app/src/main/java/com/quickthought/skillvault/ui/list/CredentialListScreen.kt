package com.quickthought.skillvault.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditCredentialSheet
import com.quickthought.skillvault.ui.list.CredentialListContract.*
import com.quickthought.skillvault.ui.list.components.BiometricAuthenticator
import com.quickthought.skillvault.ui.list.components.CredentialItem
import com.quickthought.skillvault.ui.list.components.EmptyState
import com.quickthought.skillvault.ui.list.components.LoadingState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialListScreen(
    viewModel: CredentialListViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    // we allow only one snackbar to be in the queue here, hence conflated
    val channel = remember { Channel<Int>(Channel.CONFLATED) }
    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { index ->
            ;
            val result =
                snackbarHostState.showSnackbar(
                    message = "Authentication failed.",
                    actionLabel = "Okay",
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    /* action has been performed */
                }

                SnackbarResult.Dismissed -> {
                    /* dismissed, no action needed */
                }
            }
        }
    }


    // State to control the visibility of the Add/Edit Bottom Sheet
    val showSheet = remember { mutableStateOf(false) }

    // --- Biometric Authentication Handling ---
    val biometricAuthenticator = remember { BiometricAuthenticator(context) }

    // Collect one-time events (Snackbar, Biometric Prompt)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                UiEvent.ShowBiometricPrompt -> {
                    biometricAuthenticator.prompt(
                        title = "Verify Identity",
                        subtitle = "Authenticate to reveal password",
                        onSuccess = { viewModel.handleAuthenticationSuccess() },
                        onFailure = {
                            // Show a snackbar on failure
                            channel.trySend(0)
                        }
                    )
                }
                // Handle other events like navigation here
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("SkillVault") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet.value = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Credential")
            }
        }
    ) { paddingValues ->
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
                    onItemClick = { viewModel.processAction(ViewAction.CredentialTapped(it)) },
                    onCopyClick = { viewModel.processAction(ViewAction.CopyPasswordClicked(it.credentialId)) }
                )
            }
        }
    }

    // Render the Add/Edit Bottom Sheet
    if (showSheet.value) {
         AddEditCredentialSheet(onDismiss = { showSheet.value = false })
    }
}

@Composable
fun CredentialListContent(
    credentials: List<CredentialItemUI>,
    onItemClick: (CredentialItemUI) -> Unit,
    onCopyClick: (CredentialItemUI) -> Unit
) {
    if (credentials.isEmpty()) {
        EmptyState("No credentials saved yet.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(credentials, key = { it.credentialId }) { credential ->
                CredentialItem(
                    credential = credential,
                    onClick = { onItemClick(credential) },
                    onCopyClick = { onCopyClick(credential) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
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

@Preview
@Composable
fun CredentialListScreenPreview() {
    CredentialListScreen()
}