package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.Debt
import com.example.magtipidka.domain.model.DebtType
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun getAllDebts(): Flow<List<Debt>>
    fun getDebtsByType(type: DebtType): Flow<List<Debt>>
    suspend fun getDebtById(id: Long): Debt?
    suspend fun insertDebt(debt: Debt): Long
    suspend fun updateDebt(debt: Debt)
    suspend fun recordPayment(id: Long, amount: Double)
    suspend fun deleteDebt(debt: Debt)
    suspend fun deleteDebtById(id: Long)
}
