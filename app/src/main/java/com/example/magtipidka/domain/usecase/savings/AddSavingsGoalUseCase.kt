package com.example.magtipidka.domain.usecase.savings

import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.repository.SavingsGoalRepository

class AddSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(savingsGoal: SavingsGoal): Long {
        require(savingsGoal.name.isNotBlank()) { "Goal name cannot be empty" }
        require(savingsGoal.targetAmount > 0) { "Target amount must be greater than zero" }
        return repository.insertSavingsGoal(savingsGoal)
    }
}
