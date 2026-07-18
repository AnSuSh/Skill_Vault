package com.quickthought.skillvault.ui.addedit

import app.cash.turbine.test
import com.quickthought.skillvault.data.CredentialRepository
import com.quickthought.skillvault.domain.model.CredentialItemUI
import com.quickthought.skillvault.ui.addedit.AddEditContract.UiEvent
import com.quickthought.skillvault.ui.addedit.AddEditContract.ViewAction
import com.quickthought.skillvault.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: CredentialRepository = mockk(relaxed = true)
    private lateinit var viewModel: AddEditViewModel

    @Before
    fun setup() {
        viewModel = AddEditViewModel(repository)
    }

    @After
    fun teardown() {
        // Nothing special needed for mockkStatic usually, but good to keep in mind
    }

    @Test
    fun `initial state is empty`() = runTest {
        assertEquals("", viewModel.uiState.value.accountName)
        assertEquals("", viewModel.uiState.value.username)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun `when Initialize action is triggered, state is updated`() = runTest {
        val credential = CredentialItemUI(1, "Google", "user@gmail.com")
        viewModel.processAction(ViewAction.Initialize(credential))

        assertEquals("Google", viewModel.uiState.value.accountName)
        assertEquals("user@gmail.com", viewModel.uiState.value.username)
        assertEquals(1, viewModel.uiState.value.credentialId)
        assert(viewModel.uiState.value.isEditMode)
    }

    @Test
    fun `when SaveTapped with empty fields, ShowError event is emitted`() = runTest {
        viewModel.uiEvent.test {
            viewModel.processAction(ViewAction.SaveTapped)
            val event = awaitItem()
            assert(event is UiEvent.ShowError)
        }
    }

    @Test
    fun `when SaveTapped with valid fields, repository save is called`() = runTest {
        viewModel.processAction(ViewAction.AccountNameChanged("Google"))
        viewModel.processAction(ViewAction.UsernameChanged("user"))
        viewModel.processAction(ViewAction.PasswordChanged("pass"))

        coEvery { repository.saveCredential(any(), any()) } returns Unit

        viewModel.processAction(ViewAction.SaveTapped)

        coVerify { repository.saveCredential(any(), "pass") }
    }
}
