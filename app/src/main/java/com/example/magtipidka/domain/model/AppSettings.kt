package com.example.magtipidka.domain.model

data class AppSettings(
    val currencySymbol: String = "₱",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeColorPalette: ThemeColorPalette = ThemeColorPalette.EMERALD,
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)
