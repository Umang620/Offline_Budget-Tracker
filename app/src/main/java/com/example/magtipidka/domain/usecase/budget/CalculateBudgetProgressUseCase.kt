package com.example.magtipidka.domain.usecase.budget

import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.repository.BudgetRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BudgetProgress(
    val budget: Budget,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progressPercentage: Float, // 0.0 to 1.0 (or > 1.0 if exceeded)
    val isExceeded: Boolean
)

class CalculateBudgetProgressUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    fun forOverall(month: Int, year: Int): Flow<BudgetProgress?> {
        val overallBudgetFlow = budgetRepository.getOverallBudgetForMonthYear(month, year)
        val expensesFlow = transactionRepository.getTotalExpensesByMonthYear(month, year)

        return combine(overallBudgetFlow, expensesFlow) { budget, spent ->
            if (budget == null) return@combine null
            val remaining = budget.amount - spent
            val progress = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
            BudgetProgress(
                budget = budget,
                spentAmount = spent,
                remainingAmount = remaining,
                progressPercentage = progress,
                isExceeded = spent > budget.amount
            )
        }
    }

    fun forCategoryBudgets(month: Int, year: Int): Flow<List<BudgetProgress>> {
        val budgetsFlow = budgetRepository.getBudgetsForMonthYear(month, year)
        val categoryExpensesFlow = transactionRepository.getCategoryExpensesByMonthYear(month, year)

        return combine(budgetsFlow, categoryExpensesFlow) { budgets, categoryExpenses ->
            budgets.filter { it.categoryId != null }.map { budget ->
                val categoryId = budget.categoryId!!
                val spent = categoryExpenses[categoryId] ?: 0.0
                val remaining = budget.amount - spent
                val progress = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
                BudgetProgress(
                    budget = budget,
                    spentAmount = spent,
                    remainingAmount = remaining,
                    progressPercentage = progress,
                    isExceeded = spent > budget.amount
                )
            }
        }
    }
}
