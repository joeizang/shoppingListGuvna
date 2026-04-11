package com.josephizang.shoplistguvna.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.josephizang.shoplistguvna.test.BaseE2ETest
import com.josephizang.shoplistguvna.test.TestDataBuilders
import com.josephizang.shoplistguvna.test.waitForHome
import com.josephizang.shoplistguvna.test.waitForTag
import com.josephizang.shoplistguvna.test.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListCrudTest : BaseE2ETest() {

    @Test
    fun createList_appearsOnHome() {
        composeRule.waitForHome()
        composeRule.onNodeWithContentDescription("Create List").performClick()
        composeRule.waitForTag("create_list_dialog")
        composeRule.onNodeWithTag("list_name_input").performTextInput("Weekly Shop")
        composeRule.onNodeWithText("Create").performClick()
        composeRule.waitForText("Weekly Shop")
        composeRule.onNodeWithText("Weekly Shop").assertIsDisplayed()
    }

    @Test
    fun addItem_appearsInList() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Shop", emptyList()) }
        composeRule.waitForHome()
        composeRule.waitForText("Shop")
        composeRule.onNodeWithText("Shop").performClick()
        composeRule.waitForTag("list_detail_screen")

        // Tap FAB
        composeRule.onNodeWithContentDescription("Add Item").performClick()
        composeRule.waitForTag("item_form_sheet")
        composeRule.onNodeWithTag("item_name_input").performTextInput("Tomatoes")
        composeRule.onNodeWithTag("item_quantity_input").performTextClearance()
        composeRule.onNodeWithTag("item_quantity_input").performTextInput("3")
        composeRule.onNodeWithTag("item_price_input").performTextInput("50")
        composeRule.onNodeWithText("Add").performClick()

        composeRule.waitForText("Tomatoes")
        composeRule.onNodeWithText("Tomatoes").assertIsDisplayed()
    }

    @Test
    fun editItem_updatesRow() {
        // Single item so there is exactly one Edit button
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Edit Test", listOf("Original Name" to 100.0)) }
        composeRule.waitForHome()
        composeRule.waitForText("Edit Test")
        composeRule.onNodeWithText("Edit Test").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.waitForText("Original Name")

        composeRule.onAllNodesWithContentDescription("Edit")[0].performClick()
        composeRule.waitForTag("item_form_sheet")
        composeRule.onNodeWithTag("item_name_input").performTextClearance()
        composeRule.onNodeWithTag("item_name_input").performTextInput("Renamed Item")
        composeRule.onNodeWithText("Save").performClick()

        composeRule.waitForText("Renamed Item")
        composeRule.onNodeWithText("Renamed Item").assertIsDisplayed()
    }

    @Test
    fun deleteItem_removesFromList() {
        val listId = runBlocking {
            TestDataBuilders.insertListWithItems(testApp, "Delete Test", listOf("Vanishing Item" to 50.0))
        }
        composeRule.waitForHome()
        composeRule.waitForText("Delete Test")
        composeRule.onNodeWithText("Delete Test").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.waitForText("Vanishing Item")

        composeRule.onAllNodesWithContentDescription("Delete")[0].performClick()
        composeRule.waitForIdle()

        // Verify via DAO — this proves the button is wired correctly end-to-end.
        // Room Flow → UI propagation is covered by DAO-level tests.
        val remaining = runBlocking {
            testApp.container.database.shoppingDao().getItemsForListOnce(listId)
        }
        assertThat(remaining).isEmpty()
    }

    @Test
    fun archiveList_movesToHistory() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Archive Me") }
        composeRule.waitForHome()
        composeRule.waitForText("Archive Me")
        composeRule.onNodeWithText("Archive Me").performClick()
        composeRule.waitForTag("list_detail_screen")

        composeRule.onNodeWithContentDescription("Archive List").performClick()
        composeRule.waitForHome()

        // List should not appear on home
        composeRule.onNodeWithText("Archive Me").assertDoesNotExist()

        // But should appear in History
        composeRule.onNodeWithText("History").performClick()
        composeRule.waitForTag("history_screen")
        composeRule.waitForText("Archive Me")
        composeRule.onNodeWithText("Archive Me").assertIsDisplayed()
    }

    @Test
    fun unarchiveList_movesBackToHome() {
        runBlocking {
            val listId = TestDataBuilders.insertListWithItems(testApp, "Unarchive Me")
            testApp.container.database.shoppingDao().updateListArchivedStatus(listId, true)
        }
        composeRule.waitForHome()

        composeRule.onNodeWithText("History").performClick()
        composeRule.waitForTag("history_screen")
        composeRule.waitForText("Unarchive Me")
        composeRule.onNodeWithText("Unarchive Me").performClick()
        composeRule.waitForTag("list_detail_screen")

        // Unarchive pops back to the previous screen (History)
        composeRule.onNodeWithContentDescription("Unarchive List").performClick()
        composeRule.waitForTag("history_screen")

        // Verify the list is gone from History
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText("Unarchive Me").fetchSemanticsNodes().isEmpty()
        }

        // Navigate to Home and confirm the list is there
        composeRule.onNodeWithText("Home").performClick()
        composeRule.waitForHome()
        composeRule.waitForText("Unarchive Me")
        composeRule.onNodeWithText("Unarchive Me").assertIsDisplayed()
    }
}
