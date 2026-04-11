package com.josephizang.shoplistguvna.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
class SettingsTest : BaseE2ETest() {

    private fun goToSettings() {
        composeRule.waitForHome()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForTag("settings_screen")
    }

    @Test
    fun settingsScreen_isReachable() {
        goToSettings()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }

    @Test
    fun darkModeToggle_changesState() {
        goToSettings()
        val toggle = composeRule.onNodeWithTag("dark_mode_toggle")
        // Default is dark mode on
        toggle.assertIsOn()
        toggle.performClick()
        composeRule.waitForIdle()
        toggle.assertIsOff()
        // Toggle back
        toggle.performClick()
        composeRule.waitForIdle()
        toggle.assertIsOn()
    }

    @Test
    fun totalsToggle_hidesAmountsOnListCards() {
        runBlocking { TestDataBuilders.insertListWithItems(testApp, "Totals List") }
        composeRule.waitForHome()

        // Totals visible by default — PENDING label should exist
        composeRule.waitForText("PENDING")
        composeRule.onNodeWithText("PENDING").assertIsDisplayed()

        // Navigate to settings and hide totals
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForTag("settings_screen")
        composeRule.onNodeWithTag("show_totals_toggle").assertIsOn()
        composeRule.onNodeWithTag("show_totals_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("show_totals_toggle").assertIsOff()

        // Go back to home and check amounts are masked
        composeRule.onNodeWithText("Home").performClick()
        composeRule.waitForHome()
        composeRule.waitForText("****")
        composeRule.onNodeWithText("****").assertIsDisplayed()
    }

    @Test
    fun totalsToggle_persistsAcrossNavigation() {
        goToSettings()
        composeRule.onNodeWithTag("show_totals_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("show_totals_toggle").assertIsOff()

        // Navigate away and back
        composeRule.onNodeWithText("Home").performClick()
        composeRule.waitForHome()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForTag("settings_screen")

        composeRule.onNodeWithTag("show_totals_toggle").assertIsOff()
    }
}
