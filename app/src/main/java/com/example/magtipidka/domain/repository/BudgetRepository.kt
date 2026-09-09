package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsForMonthYear(month: Int, year: Int): Flow<List<Budget>>
    fun getOverallBudgetForMonthYear(month: Int, year: Int): Flow<Budget?>
    suspend fun getBudgetById(id: Long): Budget?
    suspend fun insertOrUpdateBudget(budget: Budget): Long
    suspend fun deleteBudget(budget: Budget)
    suspend fun deleteBudgetById(id: Long)
}
