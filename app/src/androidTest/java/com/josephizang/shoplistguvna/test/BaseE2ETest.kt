package com.josephizang.shoplistguvna.test

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.preferences.core.edit
import com.josephizang.shoplistguvna.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule

abstract class BaseE2ETest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    val testApp: TestApplication
        get() = composeRule.activity.application as TestApplication

    @Before
    fun setUp() {
        runBlocking {
            testApp.container.database.clearAllTables()
            testApp.container.preferencesDataStore.edit { it.clear() }
        }
        composeRule.waitForIdle()
    }
}
