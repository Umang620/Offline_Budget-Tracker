package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.RecurringBill
import kotlinx.coroutines.flow.Flow

interface RecurringBillRepository {
    fun getAllRecurringBills(): Flow<List<RecurringBill>>
    suspend fun getDueRecurringBills(currentTime: Long): List<RecurringBill>
    suspend fun insertRecurringBill(bill: RecurringBill): Long
    suspend fun updateRecurringBill(bill: RecurringBill)
    suspend fun deleteRecurringBill(bill: RecurringBill)
    suspend fun deleteRecurringBillById(id: Long)
}
