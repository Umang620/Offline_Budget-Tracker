package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.dao.SavingsGoalDao
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingsGoalRepositoryImpl(
    private val savingsGoalDao: SavingsGoalDao
) : SavingsGoalRepository {

    override fun getAllSavingsGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getAllSavingsGoals().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSavingsGoalById(id: Long): SavingsGoal? {
        return savingsGoalDao.getSavingsGoalById(id)?.toDomain()
    }

    override suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long {
        return savingsGoalDao.insertSavingsGoal(savingsGoal.toEntity())
    }

    override suspend fun updateSavingsGoal(savingsGoal: SavingsGoal) {
        savingsGoalDao.updateSavingsGoal(savingsGoal.toEntity())
    }

    override suspend fun addContribution(goalId: Long, amount: Double) {
        savingsGoalDao.addContribution(goalId, amount)
    }

    override suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        savingsGoalDao.deleteSavingsGoal(savingsGoal.toEntity())
    }

    override suspend fun deleteSavingsGoalById(id: Long) {
        savingsGoalDao.deleteSavingsGoalById(id)
    }
}
