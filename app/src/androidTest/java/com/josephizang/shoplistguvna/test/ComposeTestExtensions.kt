package com.josephizang.shoplistguvna.test

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText

fun ComposeTestRule.waitForTag(tag: String, timeoutMs: Long = 5_000L) {
    waitUntil(timeoutMs) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
}

fun ComposeTestRule.waitForText(text: String, timeoutMs: Long = 5_000L) {
    waitUntil(timeoutMs) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

fun ComposeTestRule.waitForHome(timeoutMs: Long = 5_000L) = waitForTag("home_screen", timeoutMs)
