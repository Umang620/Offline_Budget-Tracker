package com.example.magtipidka.domain.usecase.savings

import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.repository.SavingsGoalRepository

class UpdateSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(savingsGoal: SavingsGoal) {
        require(savingsGoal.name.isNotBlank()) { "Goal name cannot be empty" }
        require(savingsGoal.targetAmount > 0) { "Target amount must be greater than zero" }
        repository.updateSavingsGoal(savingsGoal)
    }

    suspend fun addContribution(goalId: Long, amount: Double) {
        require(amount > 0) { "Contribution amount must be greater than zero" }
        repository.addContribution(goalId, amount)
    }
}
