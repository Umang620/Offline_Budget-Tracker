package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.datastore.SettingsDataStore
import com.example.magtipidka.domain.model.AppSettings
import com.example.magtipidka.domain.model.ThemeColorPalette
import com.example.magtipidka.domain.model.ThemeMode
import com.example.magtipidka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return settingsDataStore.settingsFlow
    }

    override suspend fun updateCurrencySymbol(symbol: String) {
        settingsDataStore.updateCurrencySymbol(symbol)
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        settingsDataStore.updateThemeMode(themeMode)
    }

    override suspend fun updateThemeColorPalette(palette: ThemeColorPalette) {
        settingsDataStore.updateThemeColorPalette(palette)
    }

    override suspend fun setPin(pin: String) {
        settingsDataStore.setPin(pin)
    }

    override suspend fun removePin() {
        settingsDataStore.removePin()
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val currentSettings = settingsDataStore.settingsFlow.first()
        if (!currentSettings.isPinEnabled || currentSettings.pinHash.isEmpty()) {
            return true
        }
        val inputHash = settingsDataStore.hashPin(pin)
        return inputHash == currentSettings.pinHash
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        settingsDataStore.setBiometricEnabled(enabled)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsDataStore.setNotificationsEnabled(enabled)
    }
}
