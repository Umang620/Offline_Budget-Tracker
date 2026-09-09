package com.example.magtipidka.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.magtipidka.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.*, c.name AS categoryName 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        ORDER BY t.date DESC, t.createdAt DESC
    """)
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.*, c.name AS categoryName 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.date >= :startDate AND t.date <= :endDate
        ORDER BY t.date DESC, t.createdAt DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.*, c.name AS categoryName 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.id = :id
    """)
    suspend fun getTransactionById(id: Long): TransactionWithCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("""
        SELECT t.*, c.name AS categoryName 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.description LIKE '%' || :query || '%' OR c.name LIKE '%' || :query || '%'
        ORDER BY t.date DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.*, c.name AS categoryName 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE (:type IS NULL OR t.type = :type)
          AND (:categoryId IS NULL OR t.categoryId = :categoryId)
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC
    """)
    fun filterTransactions(
        type: String?,
        categoryId: Long?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<TransactionWithCategory>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = :type")
    fun getTotalAmountByType(type: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = :type AND date >= :startDate AND date <= :endDate")
    fun getTotalAmountByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT categoryId, SUM(amount) AS totalAmount 
        FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate
        GROUP BY categoryId
    """)
    fun getCategoryExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<CategoryExpenseSum>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
