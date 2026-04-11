package com.josephizang.shoplistguvna

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.josephizang.shoplistguvna.biometric.BiometricAuthHandler
import com.josephizang.shoplistguvna.biometric.DefaultBiometricAuthHandler
import com.josephizang.shoplistguvna.data.ShoppingRepository
import com.josephizang.shoplistguvna.data.UserPreferencesRepository
import com.josephizang.shoplistguvna.data.dataStore
import com.josephizang.shoplistguvna.data.local.AppDatabase

open class AppContainer(context: Context) {

    open val splashDelayMs: Long = 2500L

    open val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    open val preferencesDataStore: DataStore<Preferences> by lazy {
        context.dataStore
    }

    open val shoppingRepository: ShoppingRepository by lazy {
        ShoppingRepository(database.shoppingDao())
    }

    open val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(preferencesDataStore)
    }

    open val biometricAuthHandler: BiometricAuthHandler = DefaultBiometricAuthHandler()
}
