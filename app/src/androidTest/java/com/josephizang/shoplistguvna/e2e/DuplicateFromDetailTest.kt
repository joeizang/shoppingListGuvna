package com.josephizang.shoplistguvna.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josephizang.shoplistguvna.test.BaseE2ETest
import com.josephizang.shoplistguvna.test.TestDataBuilders
import com.josephizang.shoplistguvna.test.waitForHome
import com.josephizang.shoplistguvna.test.waitForTag
import com.josephizang.shoplistguvna.test.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DuplicateFromDetailTest : BaseE2ETest() {

    @Test
    fun duplicateButton_isVisibleInDetailScreen() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Groceries").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").assertIsDisplayed()
    }

    @Test
    fun duplicateButton_opensDuplicateDialog() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Groceries").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForText("Duplicate List")
        composeRule.onNodeWithText("Duplicate List").assertIsDisplayed()
    }

    @Test
    fun duplicateDialog_prefillsNameWithCopySuffix() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Groceries").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForTag("duplicate_name_input")
        composeRule.onNodeWithTag("duplicate_name_input").assertTextContains("Groceries (Copy)")
    }

    @Test
    fun duplicateDialog_confirmButton_disabledWhenNameBlank() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Groceries").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForTag("duplicate_name_input")
        composeRule.onNodeWithTag("duplicate_name_input").performTextClearance()
        // Button labelled "Duplicate" inside the dialog should now be disabled
        composeRule.onNodeWithText("Duplicate").assertIsNotEnabled()
    }

    @Test
    fun duplicateDialog_cancel_doesNotCreateNewList() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "My List") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("My List").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForText("Duplicate List")
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()
        // Dialog dismissed — still on detail screen
        composeRule.onNodeWithTag("list_detail_screen").assertIsDisplayed()
        // Navigate back; only the original list should exist
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForHome()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText("My List").fetchSemanticsNodes().size == 1
        }
    }

    @Test
    fun duplicateList_fromDetail_createsNewActiveListOnHome() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Groceries").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForTag("duplicate_name_input")
        // Confirm with the pre-filled name
        composeRule.onNodeWithText("Duplicate").assertIsEnabled()
        composeRule.onNodeWithText("Duplicate").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForHome()
        composeRule.waitForText("Groceries (Copy)")
        composeRule.onNodeWithText("Groceries (Copy)").assertIsDisplayed()
        composeRule.onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun duplicateList_customName_createsListWithProvidedName() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Weekly Shop") }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Weekly Shop").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForTag("duplicate_name_input")
        composeRule.onNodeWithTag("duplicate_name_input").performTextClearance()
        composeRule.onNodeWithTag("duplicate_name_input").performTextInput("Budget List")
        composeRule.onNodeWithText("Duplicate").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForHome()
        composeRule.waitForText("Budget List")
        composeRule.onNodeWithText("Budget List").assertIsDisplayed()
    }

    @Test
    fun duplicatedList_fromDetail_containsCopiedItems() {
        runBlocking {
            TestDataBuilders.insertListWithItems(
                testApp,
                "Source List",
                listOf("Milk" to 150.0, "Eggs" to 300.0)
            )
        }
        composeRule.waitForHome()
        composeRule.onNodeWithText("Source List").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithContentDescription("Duplicate List").performClick()
        composeRule.waitForTag("duplicate_name_input")
        composeRule.onNodeWithText("Duplicate").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForHome()
        composeRule.waitForText("Source List (Copy)")
        composeRule.onNodeWithText("Source List (Copy)").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithText("Milk").assertIsDisplayed()
        composeRule.onNodeWithText("Eggs").assertIsDisplayed()
    }
}
