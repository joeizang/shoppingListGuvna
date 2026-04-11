package com.josephizang.shoplistguvna.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
class SmokeNavigationTest : BaseE2ETest() {

    @Test
    fun splashAndAuthAdvanceToHomeScreen() {
        composeRule.waitForHome()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun bottomNav_switchesToHistory() {
        composeRule.waitForHome()
        composeRule.onNodeWithText("History").performClick()
        composeRule.waitForTag("history_screen")
        composeRule.onNodeWithTag("history_screen").assertIsDisplayed()
    }

    @Test
    fun bottomNav_switchesToSettings() {
        composeRule.waitForHome()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForTag("settings_screen")
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }

    @Test
    fun bottomNav_returnsToHome() {
        composeRule.waitForHome()
        composeRule.onNodeWithText("History").performClick()
        composeRule.waitForTag("history_screen")
        composeRule.onNodeWithText("Home").performClick()
        composeRule.waitForHome()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun navigateIntoListAndBack() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "My List") }
        composeRule.waitForHome()
        composeRule.waitForText("My List")
        composeRule.onNodeWithText("My List").performClick()
        composeRule.waitForTag("list_detail_screen")
        composeRule.onNodeWithTag("list_detail_screen").assertIsDisplayed()
        composeRule.onNodeWithText("My List").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForHome()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }
}
