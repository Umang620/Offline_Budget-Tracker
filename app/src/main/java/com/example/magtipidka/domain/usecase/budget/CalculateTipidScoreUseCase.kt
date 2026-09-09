package com.example.magtipidka.domain.usecase.budget

import com.example.magtipidka.domain.model.TipidScore
import com.example.magtipidka.domain.model.TipidScoreLevel
import com.example.magtipidka.domain.repository.BudgetRepository
import com.example.magtipidka.domain.repository.DebtRepository
import com.example.magtipidka.domain.repository.SavingsGoalRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class CalculateTipidScoreUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val debtRepository: DebtRepository
) {
    operator fun invoke(): Flow<TipidScore> {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val budgetFlow = budgetRepository.getOverallBudgetForMonthYear(month, year)
        val incomeFlow = transactionRepository.getTotalIncomeByMonthYear(month, year)
        val expensesFlow = transactionRepository.getTotalExpensesByMonthYear(month, year)

        val goalsFlow = savingsGoalRepository.getAllSavingsGoals()
        val debtsFlow = debtRepository.getAllDebts()
        val balanceFlow = transactionRepository.getTotalIncome().combine(transactionRepository.getTotalExpenses()) { inc, exp ->
            inc - exp
        }

        val flowA = combine(budgetFlow, incomeFlow, expensesFlow) { budget, income, spent ->
            Triple(budget, income, spent)
        }

        val flowB = combine(goalsFlow, debtsFlow, balanceFlow) { goals, debts, balance ->
            Triple(goals, debts, balance)
        }

        return combine(flowA, flowB) { a, b ->
            val budget = a.first
            val income = a.second
            val spent = a.third

            val goals = b.first
            val debts = b.second
            val balance = b.third

            var score = 30 // Base start score

            // 1. Monthly Savings Rate Score (Income vs Expense Cashflow) - Max +40 pts
            if (income > 0) {
                val netSavings = income - spent
                val savingsRate = netSavings / income
                when {
                    savingsRate >= 0.50 -> score += 40 // Saving 50%+ of income
                    savingsRate >= 0.30 -> score += 30 // Saving 30%+ of income
                    savingsRate >= 0.10 -> score += 20 // Saving 10%+ of income
                    savingsRate >= 0.00 -> score += 10 // Positive cashflow
                    else -> score -= 25                // Net deficit (spending > income)
                }
            } else if (spent == 0.0) {
                score += 20 // No spending recorded
            } else {
                score -= 10 // Expense recorded with 0 income
            }

            // 2. Budget Control Score - Max +20 pts
            if (budget != null && budget.amount > 0) {
                val ratio = spent / budget.amount
                when {
                    ratio <= 0.75 -> score += 20 // Well under budget
                    ratio <= 1.00 -> score += 10 // Within budget
                    else -> score -= 20          // Exceeded monthly budget
                }
            } else if (balance >= 0) {
                score += 10 // Positive overall balance
            }

            // 3. Savings Momentum Score - Max +15 pts
            if (goals.isNotEmpty()) {
                val activeGoal = goals.any { it.currentAmount > 0 }
                score += if (activeGoal) 15 else 5
            }

            // 4. Debt Ratio Safety Score - Max +15 pts
            val totalUtang = debts.filter { !it.isSettled && it.type.name == "IOWE" }.sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
            when {
                totalUtang == 0.0 -> score += 15
                income > 0 && (totalUtang / income) <= 0.20 -> score += 10
                income > 0 && (totalUtang / income) > 0.50 -> score -= 15
                else -> score += 5
            }

            val finalScore = score.coerceIn(0, 100)

            val (level, msg) = when {
                finalScore >= 85 -> TipidScoreLevel.EXCELLENT to "Excellent financial discipline! High savings & zero debt!"
                finalScore >= 70 -> TipidScoreLevel.GOOD to "Great budget control! You are keeping spending low."
                finalScore >= 50 -> TipidScoreLevel.FAIR to "Fair status. Try increasing savings or setting a budget."
                else -> TipidScoreLevel.NEEDS_IMPROVEMENT to "Attention needed! High expenses or unpaid debt detected."
            }

            TipidScore(
                score = finalScore,
                level = level,
                insightMessage = msg
            )
        }
    }
}
