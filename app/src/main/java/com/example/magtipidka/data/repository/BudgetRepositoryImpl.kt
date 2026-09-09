package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.dao.BudgetDao
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgetsForMonthYear(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonthYear(month, year).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getOverallBudgetForMonthYear(month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getOverallBudgetForMonthYear(month, year).map { it?.toDomain() }
    }

    override suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)?.toDomain()
    }

    override suspend fun insertOrUpdateBudget(budget: Budget): Long {
        val existing = budgetDao.findBudget(budget.month, budget.year, budget.categoryId)
        return if (existing != null) {
            val updated = budget.copy(id = existing.id)
            budgetDao.updateBudget(updated.toEntity())
            existing.id
        } else {
            budgetDao.insertBudget(budget.toEntity())
        }
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    override suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }
}
