package com.josephizang.shoplistguvna

import android.app.Application

open class ShopListGuvnaApplication : Application() {
    open val container: AppContainer by lazy {
        AppContainer(this)
    }
}
