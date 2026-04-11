package com.josephizang.shoplistguvna.biometric

import android.content.Context
import androidx.fragment.app.FragmentActivity

interface BiometricAuthHandler {
    fun canAuthenticate(context: Context): Boolean
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    )
}
