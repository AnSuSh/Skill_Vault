package com.quickthought.skillvault.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.quickthought.skillvault.ui.list.CredentialListContent
import org.junit.Rule
import org.junit.Test

class CredentialListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun credentialList_showsEmptyState_whenNoData() {
        composeTestRule.setContent {
            CredentialListContent(
                credentials = emptyList(),
                onItemClick = {},
                onCopyClick = {},
                onDeleteClick = {},
                onConfirmDeleteClick = {},
                onDismissDeleteClick = {}
            )
        }

        // Check if our EmptyState text is visible
        composeTestRule.onNodeWithText("No credentials saved yet.").assertIsDisplayed()
    }

    @Test
    fun credentialList_showsItems_whenDataExists() {
        val dummyCredentials = listOf(
            com.quickthought.skillvault.domain.model.CredentialItemUI(1, "Google", "john.doe@gmail.com")
        )
        composeTestRule.setContent {
            CredentialListContent(
                credentials = dummyCredentials,
                onItemClick = {},
                onCopyClick = {},
                onDeleteClick = {},
                onConfirmDeleteClick = {},
                onDismissDeleteClick = {}
            )
        }

        composeTestRule.onNodeWithText("Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("john.doe@gmail.com").assertIsDisplayed()
    }
}