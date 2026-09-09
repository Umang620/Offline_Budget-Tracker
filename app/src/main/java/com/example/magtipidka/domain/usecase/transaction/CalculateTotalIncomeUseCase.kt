package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class CalculateTotalIncomeUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<Double> {
        return repository.getTotalIncome()
    }

    fun byMonthYear(month: Int, year: Int): Flow<Double> {
        return repository.getTotalIncomeByMonthYear(month, year)
    }
}
