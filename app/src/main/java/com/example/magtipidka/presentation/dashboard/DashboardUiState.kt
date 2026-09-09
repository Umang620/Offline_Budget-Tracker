package com.example.magtipidka.presentation.dashboard

import com.example.magtipidka.domain.model.CategoryExpenseShare
import com.example.magtipidka.domain.model.DailySpendLimit
import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.model.TipidScore
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.usecase.budget.BudgetProgress

data class DashboardUiState(
    val isLoading: Boolean = true,
    val currencySymbol: String = "₱",
    val currentBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val allTimeIncome: Double = 0.0,
    val allTimeExpenses: Double = 0.0,
    val totalUtang: Double = 0.0,
    val totalPautang: Double = 0.0,
    val totalBillsToPay: Double = 0.0,
    val topExpenseCategoryShare: CategoryExpenseShare? = null,
    val tipidScore: TipidScore? = null,
    val dueRecurringBillsForApproval: List<RecurringBill> = emptyList(),
    val overallBudgetProgress: BudgetProgress? = null,
    val dailySpendLimit: DailySpendLimit? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val topCategoryExpenses: List<CategoryExpenseShare> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList()
)
