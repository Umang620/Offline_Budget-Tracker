package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>
    suspend fun getSavingsGoalById(id: Long): SavingsGoal?
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)
    suspend fun addContribution(goalId: Long, amount: Double)
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)
    suspend fun deleteSavingsGoalById(id: Long)
}
