package com.example.magtipidka.presentation.recurring

import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.model.RecurringFrequency

data class RecurringBillsUiState(
    val isLoading: Boolean = true,
    val currencySymbol: String = "₱",
    val recurringBills: List<RecurringBill> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val isAddEditBillDialogOpen: Boolean = false,
    val editingBill: RecurringBill? = null,
    val titleInput: String = "",
    val amountInput: String = "",
    val categoryIdInput: Long? = null,
    val frequencyInput: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDateInput: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val errorMessage: String? = null
)
