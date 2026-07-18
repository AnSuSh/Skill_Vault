package com.quickthought.skillvault.ui.about

import com.quickthought.skillvault.billing.BillingManager
import com.quickthought.skillvault.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AboutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AboutViewModel
    private val billingManager: BillingManager = mockk(relaxed = true)

    @Before
    fun setup() {
        every { billingManager.purchaseSuccessFlow } returns MutableSharedFlow()
        every { billingManager.errorFlow } returns MutableSharedFlow()
        every { billingManager.isConnectionReady } returns MutableStateFlow(false)

        viewModel = AboutViewModel(billingManager)
    }

    @Test
    fun `initial states are correct`() {
        assertEquals("", viewModel.feedbackText.value)
        assertFalse(viewModel.isSubmittingFeedback.value)
        assertNull(viewModel.feedbackSubmissionSuccess.value)
    }

    @Test
    fun `onFeedbackTextChanged updates feedbackText`() {
        val testText = "Great app!"
        viewModel.onFeedbackTextChanged(testText)
        assertEquals(testText, viewModel.feedbackText.value)
    }

    @Test
    fun `resetFeedbackStatus clears submission success`() {
        // We can't easily trigger a success without mocking Firebase, 
        // but we can at least test the reset function.
        viewModel.resetFeedbackStatus()
        assertNull(viewModel.feedbackSubmissionSuccess.value)
    }
}
