package com.example.magtipidka

import android.app.Application
import com.example.magtipidka.di.AppContainer

class MagTipidKaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
