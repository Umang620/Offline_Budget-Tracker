package com.example.magtipidka.domain.usecase.budget

import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.repository.BudgetRepository

class DeleteBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget) {
        repository.deleteBudget(budget)
    }

    suspend operator fun invoke(id: Long) {
        repository.deleteBudgetById(id)
    }
}
