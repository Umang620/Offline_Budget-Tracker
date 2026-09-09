package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByMonthYear(month: Int, year: Int): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteTransactionById(id: Long)
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun filterTransactions(type: TransactionType?, categoryId: Long?, startDate: Long?, endDate: Long?): Flow<List<Transaction>>
    fun getTotalIncome(): Flow<Double>
    fun getTotalExpenses(): Flow<Double>
    fun getTotalIncomeByMonthYear(month: Int, year: Int): Flow<Double>
    fun getTotalExpensesByMonthYear(month: Int, year: Int): Flow<Double>
    fun getCategoryExpensesByMonthYear(month: Int, year: Int): Flow<Map<Long, Double>>
}
