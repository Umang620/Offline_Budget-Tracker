package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class CalculateTotalExpensesUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<Double> {
        return repository.getTotalExpenses()
    }

    fun byMonthYear(month: Int, year: Int): Flow<Double> {
        return repository.getTotalExpensesByMonthYear(month, year)
    }
}
