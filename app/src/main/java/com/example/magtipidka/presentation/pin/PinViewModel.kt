package com.example.magtipidka.presentation.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PinUiState(
    val isCheckingPin: Boolean = true,
    val isPinRequired: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val pinInput: String = "",
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class PinViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    init {
        checkPinRequirement()
    }

    private fun checkPinRequirement() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings().first()
            if (!settings.isPinEnabled || settings.pinHash.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isCheckingPin = false,
                    isPinRequired = false,
                    isBiometricEnabled = false,
                    isAuthenticated = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isCheckingPin = false,
                    isPinRequired = true,
                    isBiometricEnabled = settings.isBiometricEnabled,
                    isAuthenticated = false
                )
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(isAuthenticated = true)
    }

    fun onNumberClick(digit: String) {
        if (_uiState.value.pinInput.length < 4) {
            val newPin = _uiState.value.pinInput + digit
            _uiState.value = _uiState.value.copy(pinInput = newPin, errorMessage = null)
            if (newPin.length == 4) {
                verifyPin(newPin)
            }
        }
    }

    fun onDeleteClick() {
        if (_uiState.value.pinInput.isNotEmpty()) {
            val newPin = _uiState.value.pinInput.dropLast(1)
            _uiState.value = _uiState.value.copy(pinInput = newPin, errorMessage = null)
        }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val isValid = settingsRepository.verifyPin(pin)
            if (isValid) {
                _uiState.value = _uiState.value.copy(isAuthenticated = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    pinInput = "",
                    errorMessage = "Incorrect PIN. Please try again."
                )
            }
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PinViewModel(settingsRepository) as T
        }
    }
}
