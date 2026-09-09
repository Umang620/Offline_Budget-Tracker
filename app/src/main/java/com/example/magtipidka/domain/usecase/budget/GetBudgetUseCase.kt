package com.example.magtipidka.domain.usecase.budget

import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class GetBudgetUseCase(
    private val repository: BudgetRepository
) {
    fun forMonthYear(month: Int, year: Int): Flow<List<Budget>> {
        return repository.getBudgetsForMonthYear(month, year)
    }

    fun overallForMonthYear(month: Int, year: Int): Flow<Budget?> {
        return repository.getOverallBudgetForMonthYear(month, year)
    }

    suspend fun getById(id: Long): Budget? {
        return repository.getBudgetById(id)
    }
}
