package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.dao.RecurringBillDao
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.repository.RecurringBillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecurringBillRepositoryImpl(
    private val recurringBillDao: RecurringBillDao
) : RecurringBillRepository {

    override fun getAllRecurringBills(): Flow<List<RecurringBill>> {
        return recurringBillDao.getAllRecurringBills().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDueRecurringBills(currentTime: Long): List<RecurringBill> {
        return recurringBillDao.getDueRecurringBills(currentTime).map { it.toDomain() }
    }

    override suspend fun insertRecurringBill(bill: RecurringBill): Long {
        return recurringBillDao.insertRecurringBill(bill.toEntity())
    }

    override suspend fun updateRecurringBill(bill: RecurringBill) {
        recurringBillDao.updateRecurringBill(bill.toEntity())
    }

    override suspend fun deleteRecurringBill(bill: RecurringBill) {
        recurringBillDao.deleteRecurringBill(bill.toEntity())
    }

    override suspend fun deleteRecurringBillById(id: Long) {
        recurringBillDao.deleteRecurringBillById(id)
    }
}
