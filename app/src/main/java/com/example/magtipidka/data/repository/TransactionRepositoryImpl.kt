package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.dao.TransactionDao
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsByMonthYear(month: Int, year: Int): Flow<List<Transaction>> {
        val (startDate, endDate) = getMonthDateRange(month, year)
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun filterTransactions(
        type: TransactionType?,
        categoryId: Long?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Transaction>> {
        return transactionDao.filterTransactions(
            type = type?.name,
            categoryId = categoryId,
            startDate = startDate,
            endDate = endDate
        ).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTotalIncome(): Flow<Double> {
        return transactionDao.getTotalAmountByType(TransactionType.INCOME.name)
    }

    override fun getTotalExpenses(): Flow<Double> {
        return transactionDao.getTotalAmountByType(TransactionType.EXPENSE.name)
    }

    override fun getTotalIncomeByMonthYear(month: Int, year: Int): Flow<Double> {
        val (startDate, endDate) = getMonthDateRange(month, year)
        return transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, startDate, endDate)
    }

    override fun getTotalExpensesByMonthYear(month: Int, year: Int): Flow<Double> {
        val (startDate, endDate) = getMonthDateRange(month, year)
        return transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, startDate, endDate)
    }

    override fun getCategoryExpensesByMonthYear(month: Int, year: Int): Flow<Map<Long, Double>> {
        val (startDate, endDate) = getMonthDateRange(month, year)
        return transactionDao.getCategoryExpensesByDateRange(startDate, endDate).map { list ->
            list.associate { it.categoryId to it.totalAmount }
        }
    }

    private fun getMonthDateRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startDate = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val endDate = cal.timeInMillis

        return Pair(startDate, endDate)
    }
}
