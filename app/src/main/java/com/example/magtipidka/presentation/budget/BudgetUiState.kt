package com.example.magtipidka.presentation.budget

import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.usecase.budget.BudgetProgress

data class BudgetUiState(
    val isLoading: Boolean = true,
    val month: Int = 1,
    val year: Int = 2026,
    val monthName: String = "January 2026",
    val currencySymbol: String = "₱",
    val overallBudgetProgress: BudgetProgress? = null,
    val categoryBudgetProgresses: List<BudgetProgress> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val isAddEditBudgetDialogOpen: Boolean = false,
    val editingBudgetProgress: BudgetProgress? = null,
    val isCategoryBudgetMode: Boolean = false,
    val selectedCategoryId: Long? = null,
    val inputAmount: String = "",
    val errorMessage: String? = null
)
