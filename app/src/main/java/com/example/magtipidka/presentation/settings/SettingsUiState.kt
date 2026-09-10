package com.example.magtipidka.presentation.settings

import com.example.magtipidka.domain.model.ThemeColorPalette
import com.example.magtipidka.domain.model.ThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val currencySymbol: String = "₱",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeColorPalette: ThemeColorPalette = ThemeColorPalette.EMERALD,
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isSetPinDialogOpen: Boolean = false,
    val pinInput: String = "",
    val pinConfirmInput: String = "",
    val pinErrorMessage: String? = null,
    val isClearDataStep1Open: Boolean = false,
    val isClearDataStep2Open: Boolean = false,
    val backupSuccessMessage: String? = null,
    val backupErrorMessage: String? = null
)
