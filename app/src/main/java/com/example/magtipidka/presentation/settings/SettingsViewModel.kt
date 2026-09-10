package com.example.magtipidka.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.ThemeColorPalette
import com.example.magtipidka.domain.model.ThemeMode
import com.example.magtipidka.domain.repository.DataBackupRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dataBackupRepository: DataBackupRepository
) : ViewModel() {

    private val _isSetPinDialogOpen = MutableStateFlow(false)
    private val _pinInput = MutableStateFlow("")
    private val _pinConfirmInput = MutableStateFlow("")
    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    private val _isClearDataStep1Open = MutableStateFlow(false)
    private val _isClearDataStep2Open = MutableStateFlow(false)
    private val _backupSuccessMessage = MutableStateFlow<String?>(null)
    private val _backupErrorMessage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        val pinDialogStateFlow = combine(
            _isSetPinDialogOpen,
            _pinInput,
            _pinConfirmInput,
            _pinErrorMessage
        ) { open, pin, confirm, err ->
            PinDialogState(open, pin, confirm, err)
        }

        val clearDataStateFlow = combine(
            _isClearDataStep1Open,
            _isClearDataStep2Open
        ) { step1, step2 ->
            Pair(step1, step2)
        }

        combine(
            settingsRepository.getSettings(),
            pinDialogStateFlow,
            clearDataStateFlow,
            _backupSuccessMessage,
            _backupErrorMessage
        ) { settings, pinState, clearDataState, successMsg, errorMsg ->
            SettingsUiState(
                isLoading = false,
                currencySymbol = settings.currencySymbol,
                themeMode = settings.themeMode,
                themeColorPalette = settings.themeColorPalette,
                isPinEnabled = settings.isPinEnabled,
                isBiometricEnabled = settings.isBiometricEnabled,
                notificationsEnabled = settings.notificationsEnabled,
                isSetPinDialogOpen = pinState.open,
                pinInput = pinState.pin,
                pinConfirmInput = pinState.confirm,
                pinErrorMessage = pinState.err,
                isClearDataStep1Open = clearDataState.first,
                isClearDataStep2Open = clearDataState.second,
                backupSuccessMessage = successMsg,
                backupErrorMessage = errorMsg
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private data class PinDialogState(
        val open: Boolean,
        val pin: String,
        val confirm: String,
        val err: String?
    )

    fun onCurrencySymbolChanged(symbol: String) {
        viewModelScope.launch {
            settingsRepository.updateCurrencySymbol(symbol)
        }
    }

    fun onThemeModeChanged(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    fun onThemeColorPaletteChanged(palette: ThemeColorPalette) {
        viewModelScope.launch {
            settingsRepository.updateThemeColorPalette(palette)
        }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun onBiometricToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
        }
    }

    fun onPinToggled(enabled: Boolean) {
        if (enabled) {
            _pinInput.value = ""
            _pinConfirmInput.value = ""
            _pinErrorMessage.value = null
            _isSetPinDialogOpen.value = true
        } else {
            viewModelScope.launch {
                settingsRepository.removePin()
            }
        }
    }

    fun onDismissPinDialog() {
        _isSetPinDialogOpen.value = false
    }

    fun onPinInputChanged(pin: String) {
        if (pin.length <= 4) {
            _pinInput.value = pin
            _pinErrorMessage.value = null
        }
    }

    fun onPinConfirmInputChanged(confirm: String) {
        if (confirm.length <= 4) {
            _pinConfirmInput.value = confirm
            _pinErrorMessage.value = null
        }
    }

    fun onSavePin() {
        val pin = _pinInput.value
        val confirm = _pinConfirmInput.value

        if (pin.length < 4) {
            _pinErrorMessage.value = "PIN must be 4 digits long."
            return
        }

        if (pin != confirm) {
            _pinErrorMessage.value = "PINs do not match. Please try again."
            return
        }

        viewModelScope.launch {
            settingsRepository.setPin(pin)
            _isSetPinDialogOpen.value = false
        }
    }

    fun onShowClearDataStep1() {
        _isClearDataStep1Open.value = true
        _isClearDataStep2Open.value = false
    }

    fun onDismissClearDataDialogs() {
        _isClearDataStep1Open.value = false
        _isClearDataStep2Open.value = false
    }

    fun onProceedToClearDataStep2() {
        _isClearDataStep1Open.value = false
        _isClearDataStep2Open.value = true
    }

    fun onConfirmClearAllData() {
        viewModelScope.launch {
            try {
                val success = dataBackupRepository.clearAllData()
                if (success) {
                    _backupSuccessMessage.value = "All financial records have been wiped successfully."
                    _backupErrorMessage.value = null
                } else {
                    _backupErrorMessage.value = "Failed to clear application data."
                    _backupSuccessMessage.value = null
                }
            } catch (e: Exception) {
                _backupErrorMessage.value = e.message ?: "Failed to clear application data."
                _backupSuccessMessage.value = null
            } finally {
                _isClearDataStep1Open.value = false
                _isClearDataStep2Open.value = false
            }
        }
    }

    suspend fun getExportDataJson(): String {
        return dataBackupRepository.exportDataJson()
    }

    fun onImportDataJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val success = dataBackupRepository.importDataJson(jsonString)
                if (success) {
                    _backupSuccessMessage.value = "Data imported and restored successfully!"
                    _backupErrorMessage.value = null
                } else {
                    _backupErrorMessage.value = "Invalid backup data format."
                    _backupSuccessMessage.value = null
                }
            } catch (e: Exception) {
                _backupErrorMessage.value = e.message ?: "Failed to import backup data."
                _backupSuccessMessage.value = null
            }
        }
    }

    fun onClearMessages() {
        _backupSuccessMessage.value = null
        _backupErrorMessage.value = null
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val dataBackupRepository: DataBackupRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                settingsRepository,
                dataBackupRepository
            ) as T
        }
    }
}
