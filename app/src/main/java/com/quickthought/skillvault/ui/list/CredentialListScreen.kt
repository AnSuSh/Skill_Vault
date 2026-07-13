package com.quickthought.skillvault.ui.list

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickthought.skillvault.R
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditCredentialHost
import com.quickthought.skillvault.ui.list.CredentialListContract.UiEvent
import com.quickthought.skillvault.ui.list.CredentialListContract.UiState
import com.quickthought.skillvault.ui.list.CredentialListContract.ViewAction
import com.quickthought.skillvault.ui.list.components.BiometricAuthenticator
import com.quickthought.skillvault.ui.list.components.CredentialItem
import com.quickthought.skillvault.ui.list.components.EmptyState
import com.quickthought.skillvault.ui.list.components.LoadingState
import com.quickthought.skillvault.ui.list.components.copyTextToClipboard
import com.quickthought.skillvault.ui.widgets.ConfirmationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CredentialListScreen(
    viewModel: CredentialListViewModel = hiltViewModel(),
    onAboutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState()

    CredentialListScreenContent(
        state = state,
        searchQuery = searchQuery,
        uiEvent = viewModel.uiEvent,
        processAction = { viewModel.processAction(it) },
        onAuthenticationSuccess = { viewModel.handleAuthenticationSuccess() },
        onAuthenticationFailure = { viewModel.showErrorMessage(it) },
        onAboutClick = onAboutClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialListScreenContent(
    state: UiState,
    searchQuery: String,
    uiEvent: Flow<UiEvent>,
    processAction: (ViewAction) -> Unit,
    onAuthenticationSuccess: () -> Unit,
    onAuthenticationFailure: (String) -> Unit,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    // Holds the Credential object to be edited (null if adding a new one)
    val credentialToEdit = remember { mutableStateOf<CredentialItemUI?>(null) }

    // State to control the visibility of the Add/Edit Bottom Sheet
    val showSheet = remember { mutableStateOf(false) }

    // haptic feedback
    val sendHaptic = remember { mutableStateOf(false) }

    // --- Biometric Authentication Handling ---
    val isInspectionMode = LocalInspectionMode.current
    val biometricAuthenticator = remember {
        if (isInspectionMode) null else BiometricAuthenticator(context)
    }

    val searchActive = remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Keep list / grid states to detect scroll position
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // Show FAB unless the list/grid is at the end (last item visible). It will also be visible when at top.
    val showFab by remember {
        derivedStateOf {
            val totalItems = when (val s = state) {
                is UiState.Success -> s.credentials.size
                else -> 0
            }
            if (totalItems == 0) {
                // Always show if the list is empty.
                return@derivedStateOf true
            }

            val canScroll =
                if (isLandscape) gridState.canScrollForward else listState.canScrollForward

            // If the list can scroll, we show the FAB. It will only be false at the very end.
            // If the list CANNOT scroll (i.e., all items are visible), we also show the FAB.
            val allItemsVisible = if (isLandscape) {
                gridState.layoutInfo.visibleItemsInfo.size == totalItems
            } else {
                listState.layoutInfo.visibleItemsInfo.size == totalItems
            }

            if (allItemsVisible) {
                true // All items fit on screen, so show the FAB.
            } else {
                canScroll // For long lists, visibility depends on whether we can still scroll down.
            }
        }
    }


    // Collect one-time events (Snackbar, Biometric Prompt)
    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is UiEvent.CopyToClipBoard -> {
                    copyTextToClipboard(context, event.password)
                    sendHaptic.value = true
                }

                UiEvent.ShowBiometricPrompt -> {
                    biometricAuthenticator?.prompt(
                        onSuccess = { onAuthenticationSuccess() },
                        onFailure = { onAuthenticationFailure("Authentication failed.") }
                    )
                }

                UiEvent.OpenAddSheet -> {
                    credentialToEdit.value = null
                    showSheet.value = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    AnimatedVisibility(visible = !searchActive.value) {
                        TopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            },
                            actions = {
                                IconButton(onClick = { searchActive.value = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                }
                                IconButton(
                                    onClick = onAboutClick
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "About Us"
                                    )
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = searchActive.value) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { processAction(ViewAction.SearchQueryChanged(it)) },
                            onSearch = { searchActive.value = false },
                            active = searchActive.value,
                            onActiveChange = { searchActive.value = it },
                            placeholder = { Text(stringResource(R.string.search_accounts)) },
                            leadingIcon = {
                                if (searchActive.value) {
                                    IconButton(onClick = { searchActive.value = false }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            },
                            trailingIcon = {
                                if (searchActive.value && searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        processAction(
                                            ViewAction.SearchQueryChanged(
                                                ""
                                            )
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = stringResource(R.string.clear_search)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = if (searchActive.value) 0.dp else 16.dp,
                                    vertical = if (searchActive.value) 0.dp else 8.dp
                                ),
                        ) {
                            if (state is UiState.Success) {
                                CredentialListContent(
                                    credentials = state.credentials,
                                    isLandscape = isLandscape,
                                    onItemClick = {
                                        showSheet.value = true
                                        credentialToEdit.value = it
                                        searchActive.value = false
                                    },
                                    onCopyClick = {
                                        processAction(
                                            ViewAction.CopyPasswordClicked(
                                                it.credentialId
                                            )
                                        )
                                    },
                                    onDeleteClick = {
                                        processAction(
                                            ViewAction.DeleteIconClicked(
                                                it
                                            )
                                        )
                                    },
                                    onConfirmDeleteClick = { processAction(ViewAction.ConfirmDelete) },
                                    onDismissDeleteClick = { processAction(ViewAction.DismissDeleteDialog) },
                                    pendingDeleteId = state.pendingDeleteId
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showFab,
                ) {
                    LargeExtendedFloatingActionButton(
                        onClick = {
                            credentialToEdit.value = null // Ensure model is null when adding new
                            showSheet.value = true
                        }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_new_credential)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { paddingValues ->

            if (sendHaptic.value) {
                val haptic = LocalHapticFeedback.current
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                LaunchedEffect(Unit) {
                    delay(1000.milliseconds)
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
                    is UiState.Error -> ErrorState(state.message)
                    is UiState.Success -> CredentialListContent(
                        credentials = state.credentials,
                        isLandscape = isLandscape,
                        listState = listState,
                        gridState = gridState,
                        onItemClick = {
                            showSheet.value = true
                            credentialToEdit.value = it
                        },
                        onCopyClick = {
                            processAction(
                                ViewAction.CopyPasswordClicked(
                                    it.credentialId
                                )
                            )
                        },
                        onDeleteClick = { processAction(ViewAction.DeleteIconClicked(it)) },
                        onConfirmDeleteClick = { processAction(ViewAction.ConfirmDelete) },
                        onDismissDeleteClick = { processAction(ViewAction.DismissDeleteDialog) },
                        pendingDeleteId = state.pendingDeleteId
                    )
                }
            }
        }
    }

    // Render the Add/Edit Bottom Sheet
    if (showSheet.value) {
        AddEditCredentialHost(
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
    isLandscape: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    onItemClick: (CredentialItemUI) -> Unit,
    onCopyClick: (CredentialItemUI) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onConfirmDeleteClick: () -> Unit,
    onDismissDeleteClick: () -> Unit,
    pendingDeleteId: Int? = null
) {
    if (credentials.isEmpty()) {
        EmptyState(stringResource(R.string.no_credentials_saved_yet))
    } else {
        if (isLandscape) {
            // Use LazyVerticalGrid for landscape orientation
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 items per row
                contentPadding = PaddingValues(16.dp),
                state = gridState
            ) {
                items(credentials, key = { it.credentialId }) { credential ->
                    CredentialItem(
                        credential = credential,
                        onClick = { onItemClick(credential) },
                        onCopyClick = { onCopyClick(credential) },
                        onDeleteClick = { onDeleteClick(credential.credentialId) },
                        modifier = Modifier.padding(4.dp) // Add some padding for grid items
                    )
                }
            }
        } else {
            // Use LazyColumn for portrait orientation (your existing code)
            LazyColumn(contentPadding = PaddingValues(16.dp), state = listState) {
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
        }

        if (pendingDeleteId != null) {
            ConfirmationDialog(
                title = stringResource(R.string.delete_password_alert),
                text = stringResource(R.string.irreversible_action_warning),
                onConfirm = { onConfirmDeleteClick() },
                onDismiss = { onDismissDeleteClick() }
            )
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Text(
        text = stringResource(R.string.error, message),
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

@Preview(showBackground = true, name = "Credential List Screen - Success")
@Composable
fun CredentialListScreenPreview() {
    val dummyCredentials = listOf(
        CredentialItemUI(1, "Google", "john.doe@gmail.com"),
        CredentialItemUI(2, "Facebook", "john.d"),
        CredentialItemUI(3, "Twitter", "@john_doe")
    )
    MaterialTheme {
        CredentialListScreenContent(
            state = UiState.Success(credentials = dummyCredentials),
            searchQuery = "",
            uiEvent = emptyFlow(),
            processAction = {},
            onAuthenticationSuccess = {},
            onAuthenticationFailure = {},
            onAboutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Credential List Screen - Empty")
@Composable
fun CredentialListScreenEmptyPreview() {
    MaterialTheme {
        CredentialListScreenContent(
            state = UiState.Success(credentials = emptyList()),
            searchQuery = "",
            uiEvent = emptyFlow(),
            processAction = {},
            onAuthenticationSuccess = {},
            onAuthenticationFailure = {},
            onAboutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Credential List Screen - Loading")
@Composable
fun CredentialListScreenLoadingPreview() {
    MaterialTheme {
        CredentialListScreenContent(
            state = UiState.Loading,
            searchQuery = "",
            uiEvent = emptyFlow(),
            processAction = {},
            onAuthenticationSuccess = {},
            onAuthenticationFailure = {},
            onAboutClick = {}
        )
    }
}
