package com.example.magtipidka.presentation.reports

import com.example.magtipidka.domain.model.ReportData

data class ReportsUiState(
    val isLoading: Boolean = true,
    val month: Int = 1,
    val year: Int = 2026,
    val monthName: String = "January 2026",
    val currencySymbol: String = "₱",
    val reportData: ReportData? = null
)
