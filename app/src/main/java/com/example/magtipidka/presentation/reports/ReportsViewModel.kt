package com.example.magtipidka.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.usecase.report.GenerateMonthlyReportUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(
    private val generateMonthlyReportUseCase: GenerateMonthlyReportUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val _month = MutableStateFlow(cal.get(Calendar.MONTH) + 1)
    private val _year = MutableStateFlow(cal.get(Calendar.YEAR))

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        observeReportData()
    }

    private fun observeReportData() {
        val monthYearFlow = combine(_month, _year) { m, y -> Pair(m, y) }

        val reportFlow = monthYearFlow.flatMapLatest { (m, y) ->
            generateMonthlyReportUseCase(m, y)
        }

        combine(
            monthYearFlow,
            reportFlow,
            settingsRepository.getSettings()
        ) { (m, y), report, settings ->
            val monthNames = DateFormatSymbols(Locale.ENGLISH).months
            val mName = "${monthNames.getOrElse(m - 1) { "" }} $y"

            ReportsUiState(
                isLoading = false,
                month = m,
                year = y,
                monthName = mName,
                currencySymbol = settings.currencySymbol,
                reportData = report
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onPreviousMonth() {
        if (_month.value == 1) {
            _month.value = 12
            _year.value -= 1
        } else {
            _month.value -= 1
        }
    }

    fun onNextMonth() {
        if (_month.value == 12) {
            _month.value = 1
            _year.value += 1
        } else {
            _month.value += 1
        }
    }

    class Factory(
        private val generateMonthlyReportUseCase: GenerateMonthlyReportUseCase,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(
                generateMonthlyReportUseCase,
                settingsRepository
            ) as T
        }
    }
}
