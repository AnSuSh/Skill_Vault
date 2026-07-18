package com.quickthought.skillvault.list

import app.cash.turbine.test
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.list.CredentialListContract
import com.quickthought.skillvault.ui.list.CredentialListViewModel
import com.quickthought.skillvault.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CredentialListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: CredentialRepository = mockk(relaxed = true)
    private lateinit var viewModel: CredentialListViewModel

    @Before
    fun setup() {
        viewModel = CredentialListViewModel(repository)
    }

    @Test
    fun `when Copy Action is triggered, ShowBiometricPrompt event is emitted`() = runTest {
        viewModel.uiEvent.test {
            // Act
            viewModel.processAction(CredentialListContract.ViewAction.CopyPasswordClicked(1))

            // Assert
            val event = awaitItem()
            assert(event is CredentialListContract.UiEvent.ShowBiometricPrompt)
        }
    }

    @Test
    fun `when search query is entered, uiState emits filtered credentials`() = runTest {
        // Arrange
        val list = listOf(
            CredentialItemUI(1, "Google", "user1"),
            CredentialItemUI(2, "Netflix", "user2")
        )
        coEvery { repository.getCredentials() } returns flowOf(list)

        viewModel.uiState.test {
            // Consume the initial Success(empty) or Success(list) state if loadCredentials was called in init
            // Actually, CredentialListViewModel calls loadCredentials() in init.
            
            // Act
            viewModel.processAction(CredentialListContract.ViewAction.SearchQueryChanged("Net"))

            // Skip initial states
            skipItems(1) 

            val state = awaitItem() as CredentialListContract.UiState.Success
            assertEquals(1, state.credentials.size)
            assertEquals("Netflix", state.credentials[0].accountName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when handleShortcutAction is called with add_new, OpenAddSheet is emitted`() = runTest {
        viewModel.uiEvent.test {
            viewModel.handleShortcutAction("add_new")
            val event = awaitItem()
            assert(event is CredentialListContract.UiEvent.OpenAddSheet)
        }
    }
}
