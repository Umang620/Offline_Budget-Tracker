package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.AppSettings
import com.example.magtipidka.domain.model.ThemeColorPalette
import com.example.magtipidka.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateCurrencySymbol(symbol: String)
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateThemeColorPalette(palette: ThemeColorPalette)
    suspend fun setPin(pin: String)
    suspend fun removePin()
    suspend fun verifyPin(pin: String): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
