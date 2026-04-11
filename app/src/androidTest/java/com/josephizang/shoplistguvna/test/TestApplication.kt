package com.josephizang.shoplistguvna.test

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import com.josephizang.shoplistguvna.AppContainer
import com.josephizang.shoplistguvna.ShopListGuvnaApplication
import com.josephizang.shoplistguvna.biometric.BiometricAuthHandler
import com.josephizang.shoplistguvna.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

class TestApplication : ShopListGuvnaApplication() {

    override val container: AppContainer by lazy {
        object : AppContainer(this) {

            override val splashDelayMs: Long = 0L

            override val database: AppDatabase by lazy {
                // Do NOT use allowMainThreadQueries here — it breaks Room's
                // InvalidationTracker background thread and Flow collectors never fire.
                Room.inMemoryDatabaseBuilder(
                    this@TestApplication,
                    AppDatabase::class.java
                ).build()
            }

            override val preferencesDataStore: DataStore<Preferences> by lazy {
                PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                    produceFile = { File(filesDir, "test_settings.preferences_pb") }
                )
            }

            override val biometricAuthHandler: BiometricAuthHandler =
                object : BiometricAuthHandler {
                    override fun canAuthenticate(context: Context): Boolean = false
                    override fun authenticate(
                        activity: FragmentActivity,
                        onSuccess: () -> Unit,
                        onFailure: (String) -> Unit
                    ) = onSuccess()
                }
        }
    }
}
