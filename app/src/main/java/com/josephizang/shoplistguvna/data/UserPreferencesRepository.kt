package com.josephizang.shoplistguvna.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val IS_TOTALS_VISIBLE = booleanPreferencesKey("is_totals_visible")

    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: true
        }

    val isTotalsVisible: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[IS_TOTALS_VISIBLE] ?: true
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    suspend fun setTotalsVisible(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_TOTALS_VISIBLE] = enabled
        }
    }
}
