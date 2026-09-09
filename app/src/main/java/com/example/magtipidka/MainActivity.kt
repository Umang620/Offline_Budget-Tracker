package com.example.magtipidka

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.magtipidka.domain.model.AppSettings
import com.example.magtipidka.presentation.navigation.MainNavGraph
import com.example.magtipidka.presentation.theme.MagTipidKaTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MagTipidKaApplication
        val container = app.container

        setContent {
            val settingsState by container.settingsRepository.getSettings().collectAsState(
                initial = AppSettings()
            )

            MagTipidKaTheme(
                themeMode = settingsState.themeMode,
                themeColorPalette = settingsState.themeColorPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavGraph(container = container)
                }
            }
        }
    }
}
