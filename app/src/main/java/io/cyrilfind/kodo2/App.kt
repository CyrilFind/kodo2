package io.cyrilfind.kodo2

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.setUpContext(this)
    }
}