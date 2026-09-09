package com.example.magtipidka.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.magtipidka.domain.model.AppSettings
import com.example.magtipidka.domain.model.ThemeColorPalette
import com.example.magtipidka.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val THEME_COLOR_PALETTE_KEY = stringPreferencesKey("theme_color_palette")
        val IS_PIN_ENABLED_KEY = booleanPreferencesKey("is_pin_enabled")
        val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
        val IS_BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("is_biometric_enabled")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val currency = preferences[CURRENCY_SYMBOL_KEY] ?: "₱"
        val themeStr = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        val paletteStr = preferences[THEME_COLOR_PALETTE_KEY] ?: ThemeColorPalette.EMERALD.name
        val themeColorPalette = try {
            ThemeColorPalette.valueOf(paletteStr)
        } catch (e: Exception) {
            ThemeColorPalette.EMERALD
        }
        val isPinEnabled = preferences[IS_PIN_ENABLED_KEY] ?: false
        val pinHash = preferences[PIN_HASH_KEY] ?: ""
        val isBiometricEnabled = preferences[IS_BIOMETRIC_ENABLED_KEY] ?: false
        val notifications = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true

        AppSettings(
            currencySymbol = currency,
            themeMode = themeMode,
            themeColorPalette = themeColorPalette,
            isPinEnabled = isPinEnabled,
            pinHash = pinHash,
            isBiometricEnabled = isBiometricEnabled,
            notificationsEnabled = notifications
        )
    }

    suspend fun updateCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] = symbol
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun updateThemeColorPalette(palette: ThemeColorPalette) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_PALETTE_KEY] = palette.name
        }
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        context.dataStore.edit { preferences ->
            preferences[PIN_HASH_KEY] = hash
            preferences[IS_PIN_ENABLED_KEY] = true
        }
    }

    suspend fun removePin() {
        context.dataStore.edit { preferences ->
            preferences[PIN_HASH_KEY] = ""
            preferences[IS_PIN_ENABLED_KEY] = false
            preferences[IS_BIOMETRIC_ENABLED_KEY] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
