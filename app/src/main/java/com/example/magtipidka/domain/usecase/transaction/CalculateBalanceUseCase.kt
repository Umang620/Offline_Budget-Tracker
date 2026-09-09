package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CalculateBalanceUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<Double> {
        return combine(
            repository.getTotalIncome(),
            repository.getTotalExpenses()
        ) { income, expense ->
            income - expense
        }
    }

    fun byMonthYear(month: Int, year: Int): Flow<Double> {
        return combine(
            repository.getTotalIncomeByMonthYear(month, year),
            repository.getTotalExpensesByMonthYear(month, year)
        ) { income, expense ->
            income - expense
        }
    }
}
