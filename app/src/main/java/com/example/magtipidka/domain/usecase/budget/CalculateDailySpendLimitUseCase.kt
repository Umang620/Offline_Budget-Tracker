package com.example.magtipidka.domain.usecase.budget

import com.example.magtipidka.domain.model.DailyLimitStatus
import com.example.magtipidka.domain.model.DailySpendLimit
import com.example.magtipidka.domain.repository.BudgetRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class CalculateDailySpendLimitUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<DailySpendLimit?> {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDays = (maxDaysInMonth - currentDay + 1).coerceAtLeast(1)

        val overallBudgetFlow = budgetRepository.getOverallBudgetForMonthYear(currentMonth, currentYear)
        val expensesFlow = transactionRepository.getTotalExpensesByMonthYear(currentMonth, currentYear)

        return combine(overallBudgetFlow, expensesFlow) { budget, spent ->
            if (budget == null || budget.amount <= 0) return@combine null

            val remaining = budget.amount - spent
            val safeDaily = if (remaining > 0) remaining / remainingDays else 0.0

            val status = when {
                spent > budget.amount -> DailyLimitStatus.EXCEEDED
                safeDaily < 150.0 -> DailyLimitStatus.WARNING
                else -> DailyLimitStatus.SAFE
            }

            DailySpendLimit(
                safeDailyLimit = safeDaily,
                remainingBudget = remaining,
                daysRemainingInMonth = remainingDays,
                status = status
            )
        }
    }
}
