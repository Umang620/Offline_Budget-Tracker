package com.example.magtipidka.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.magtipidka.data.local.entity.RecurringBillEntity
import kotlinx.coroutines.flow.Flow

data class RecurringBillWithCategory(
    val id: Long,
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String?,
    val frequency: String,
    val nextDueDate: Long,
    val isAutoAdd: Boolean,
    val createdAt: Long
)

@Dao
interface RecurringBillDao {
    @Query("""
        SELECT r.id, r.title, r.amount, r.categoryId, c.name AS categoryName, r.frequency, r.nextDueDate, r.isAutoAdd, r.createdAt 
        FROM recurring_bills r 
        LEFT JOIN categories c ON r.categoryId = c.id 
        ORDER BY r.nextDueDate ASC
    """)
    fun getAllRecurringBills(): Flow<List<RecurringBillWithCategory>>

    @Query("SELECT * FROM recurring_bills WHERE nextDueDate <= :currentTime")
    suspend fun getDueRecurringBills(currentTime: Long): List<RecurringBillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringBill(bill: RecurringBillEntity): Long

    @Update
    suspend fun updateRecurringBill(bill: RecurringBillEntity)

    @Delete
    suspend fun deleteRecurringBill(bill: RecurringBillEntity)

    @Query("DELETE FROM recurring_bills WHERE id = :id")
    suspend fun deleteRecurringBillById(id: Long)

    @Query("DELETE FROM recurring_bills")
    suspend fun deleteAllRecurringBills()
}
