package com.example.magtipidka.domain.usecase.savings

import com.example.magtipidka.domain.model.SavingsGoal

data class SavingsGoalProgress(
    val goal: SavingsGoal,
    val remainingAmount: Double,
    val progressPercentage: Float, // 0.0 to 1.0 (or 100%)
    val isCompleted: Boolean
)

class CalculateSavingsProgressUseCase {
    operator fun invoke(goal: SavingsGoal): SavingsGoalProgress {
        val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
        val progress = if (goal.targetAmount > 0) {
            (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f
        return SavingsGoalProgress(
            goal = goal,
            remainingAmount = remaining,
            progressPercentage = progress,
            isCompleted = goal.currentAmount >= goal.targetAmount
        )
    }
}
