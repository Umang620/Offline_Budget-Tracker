package com.example.magtipidka

import android.app.Application
import com.example.magtipidka.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MagTipidKaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Eagerly seed default categories in background on launch if DB was cleared
        CoroutineScope(Dispatchers.IO).launch {
            container.categoryRepository.insertDefaultCategoriesIfNeeded()
        }
    }
}
