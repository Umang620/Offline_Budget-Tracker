package com.example.magtipidka.domain.usecase.savings

import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.repository.SavingsGoalRepository

class DeleteSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(savingsGoal: SavingsGoal) {
        repository.deleteSavingsGoal(savingsGoal)
    }

    suspend operator fun invoke(id: Long) {
        repository.deleteSavingsGoalById(id)
    }
}
