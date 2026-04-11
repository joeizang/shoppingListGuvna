package com.josephizang.shoplistguvna.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class DuplicateListTest : BaseE2ETest() {

    @Test
    fun overflowMenu_visibleOnPopulatedListCard() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.waitForText("Groceries")
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForText("Duplicate")
        composeRule.onNodeWithText("Duplicate").assertIsDisplayed()
    }

    @Test
    fun duplicateOption_enabledForPopulatedList() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Full List") }
        composeRule.waitForHome()
        composeRule.waitForText("Full List")
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForText("Duplicate")
        composeRule.onNodeWithText("Duplicate").assertIsEnabled()
    }

    @Test
    fun duplicateOption_disabledForEmptyList() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Empty List", emptyList()) }
        composeRule.waitForHome()
        composeRule.waitForText("Empty List")
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForText("Duplicate")
        composeRule.onNodeWithText("Duplicate").assertIsNotEnabled()
    }

    @Test
    fun duplicatingPopulatedList_createsActiveCopy() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Groceries") }
        composeRule.waitForHome()
        composeRule.waitForText("Groceries")
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForText("Duplicate")
        composeRule.onNodeWithText("Duplicate").performClick()
        composeRule.waitForIdle()

        // Both original and copy should appear on home
        composeRule.waitForText("Groceries (copy)")
        composeRule.onNodeWithText("Groceries (copy)").assertIsDisplayed()
        composeRule.onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun duplicatedList_containsCopiedItems() {
        runBlocking {
            TestDataBuilders.insertListWithItems(
                testApp,
                "Source",
                listOf("Apple" to 100.0, "Bread" to 200.0)
            )
        }
        composeRule.waitForHome()
        composeRule.waitForText("Source")
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForText("Duplicate")
        composeRule.onNodeWithText("Duplicate").performClick()
        composeRule.waitForText("Source (copy)")

        composeRule.onNodeWithText("Source (copy)").performClick()
        composeRule.waitForTag("list_detail_screen")

        composeRule.onNodeWithText("Apple").assertIsDisplayed()
        composeRule.onNodeWithText("Bread").assertIsDisplayed()
    }

    @Test
    fun deleteFromOverflow_removesCard() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Delete Me") }
        composeRule.waitForHome()
        composeRule.waitForText("Delete Me")
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.waitForText("Delete")
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Delete Me").assertDoesNotExist()
    }
}
