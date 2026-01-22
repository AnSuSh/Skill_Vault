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
    private val credUIDummy = CredentialItemUI(1, "Google", "john.doe@gmail.com")
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

            // Assert (Turbine makes this very readable)
            val event = awaitItem()
            assert(event is CredentialListContract.UiEvent.ShowBiometricPrompt)
        }
    }

//    @Test
//    fun `when Credential is tapped, CredentialTapped event is emitted`() = runTest {
//        viewModel.uiEvent.test {
//            viewModel.processAction(CredentialListContract.ViewAction.CredentialTapped(credUIDummy))
//
//            val event = awaitItem()
//            assert(event is CredentialListContract.UiEvent.ShowSnackbar)
//        }
//    }

    @Test
    fun `when search query is entered, uiState emits filtered credentials`() = runTest {
        // 1. Arrange: Setup the repository to return a list of 2 items
        val list = listOf(
            CredentialItemUI(1, "Google", "user1"),
            CredentialItemUI(2, "Netflix", "user2")
        )
        coEvery { repository.getCredentials() } returns flowOf(list)

        // 3. Assert: Verify the state only contains Netflix
        viewModel.uiState.test {
            // 2. Act: Trigger the search (This will fail to compile!)
            viewModel.processAction(CredentialListContract.ViewAction.SearchQueryChanged("Net"))

            // Consume the initial Loading state
            val loadingState = awaitItem()
            Assert.assertTrue(loadingState is CredentialListContract.UiState.Loading)

            val state = awaitItem() as CredentialListContract.UiState.Success
            assertEquals(1, state.credentials.size)
            assertEquals("Netflix", state.credentials[0].accountName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    /* The Implementation is modified.

    @Test
    fun `when Delete action is triggered, repository delete is called`() = runTest {
        // 1. Arrange
        val credentialIdToDelete = 1
        // We mock the repository to just "successfully" return when delete is called
        coEvery { repository.deleteCredential(any()) } returns Unit

        // 2. Act
        // This will FAIL TO COMPILE because 'deleteCredential' isn't in our ViewAction yet
        viewModel.processAction(CredentialListContract.ViewAction.DeleteIconClicked(credentialIdToDelete))

        // 3. Assert
        // Verify the repository was actually told to delete this specific ID
        coVerify(exactly = 1) { repository.deleteCredential(credentialIdToDelete) }
    }*/

    /* The Implementation is modified.

    @Test
    fun `when Delete fails, ShowSnackbar event is emitted with error message`() = runTest {
        // Arrange
        coEvery { repository.deleteCredential(any()) } throws Exception("Database Error")

        viewModel.uiEvent.test {
            // Act
            viewModel.processAction(CredentialListContract.ViewAction.DeleteIconClicked(1))

            // Assert
            val event = awaitItem()
            assert(event is CredentialListContract.UiEvent.ShowSnackbar)
            assert((event as CredentialListContract.UiEvent.ShowSnackbar).message.contains("Failed to delete"))
        }
    }
    */
}