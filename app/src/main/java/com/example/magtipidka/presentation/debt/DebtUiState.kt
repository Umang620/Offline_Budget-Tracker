package com.example.magtipidka.presentation.debt

import com.example.magtipidka.domain.model.Debt
import com.example.magtipidka.domain.model.DebtType

data class DebtUiState(
    val isLoading: Boolean = true,
    val currencySymbol: String = "₱",
    val selectedFilterType: DebtType? = null, // null for ALL
    val debts: List<Debt> = emptyList(),
    val isAddEditDebtDialogOpen: Boolean = false,
    val editingDebt: Debt? = null,
    val personNameInput: String = "",
    val amountInput: String = "",
    val typeInput: DebtType = DebtType.IOWE,
    val dueDateInput: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000), // Default +7 days
    val noteInput: String = "",
    val isRecordPaymentDialogOpen: Boolean = false,
    val payingDebt: Debt? = null,
    val paymentAmountInput: String = "",
    val errorMessage: String? = null
)
