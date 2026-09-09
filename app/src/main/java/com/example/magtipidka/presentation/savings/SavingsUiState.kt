package com.example.magtipidka.presentation.savings

import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.usecase.savings.SavingsGoalProgress

data class SavingsUiState(
    val isLoading: Boolean = true,
    val currencySymbol: String = "₱",
    val savingsProgresses: List<SavingsGoalProgress> = emptyList(),
    val isAddEditGoalDialogOpen: Boolean = false,
    val editingGoal: SavingsGoal? = null,
    val goalNameInput: String = "",
    val targetAmountInput: String = "",
    val currentAmountInput: String = "",
    val targetDateInput: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Default +30 days
    val isAddContributionDialogOpen: Boolean = false,
    val contributingGoal: SavingsGoal? = null,
    val contributionAmountInput: String = "",
    val errorMessage: String? = null
)
