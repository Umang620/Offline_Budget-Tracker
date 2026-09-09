package com.example.magtipidka.domain.usecase.budget

import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.repository.BudgetRepository

class SetBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Long {
        require(budget.amount > 0) { "Budget amount must be greater than zero" }
        require(budget.month in 1..12) { "Invalid month" }
        require(budget.year > 2000) { "Invalid year" }
        return repository.insertOrUpdateBudget(budget)
    }
}
