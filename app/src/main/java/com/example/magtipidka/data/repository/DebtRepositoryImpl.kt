package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.dao.DebtDao
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.Debt
import com.example.magtipidka.domain.model.DebtType
import com.example.magtipidka.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DebtRepositoryImpl(
    private val debtDao: DebtDao
) : DebtRepository {

    override fun getAllDebts(): Flow<List<Debt>> {
        return debtDao.getAllDebts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getDebtsByType(type: DebtType): Flow<List<Debt>> {
        return debtDao.getDebtsByType(type.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDebtById(id: Long): Debt? {
        return debtDao.getDebtById(id)?.toDomain()
    }

    override suspend fun insertDebt(debt: Debt): Long {
        return debtDao.insertDebt(debt.toEntity())
    }

    override suspend fun updateDebt(debt: Debt) {
        debtDao.updateDebt(debt.toEntity())
    }

    override suspend fun recordPayment(id: Long, amount: Double) {
        debtDao.recordPayment(id, amount)
    }

    override suspend fun deleteDebt(debt: Debt) {
        debtDao.deleteDebt(debt.toEntity())
    }

    override suspend fun deleteDebtById(id: Long) {
        debtDao.deleteDebtById(id)
    }
}
