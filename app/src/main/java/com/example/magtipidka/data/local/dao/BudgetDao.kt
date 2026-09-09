package com.example.magtipidka.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.magtipidka.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

data class BudgetWithCategory(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val amount: Double,
    val month: Int,
    val year: Int
)

@Dao
interface BudgetDao {
    @Query("""
        SELECT b.id, b.categoryId, c.name AS categoryName, b.amount, b.month, b.year 
        FROM budgets b 
        LEFT JOIN categories c ON b.categoryId = c.id
    """)
    fun getAllBudgets(): Flow<List<BudgetWithCategory>>

    @Query("""
        SELECT b.id, b.categoryId, c.name AS categoryName, b.amount, b.month, b.year 
        FROM budgets b 
        LEFT JOIN categories c ON b.categoryId = c.id 
        WHERE b.month = :month AND b.year = :year
    """)
    fun getBudgetsForMonthYear(month: Int, year: Int): Flow<List<BudgetWithCategory>>

    @Query("""
        SELECT b.id, b.categoryId, c.name AS categoryName, b.amount, b.month, b.year 
        FROM budgets b 
        LEFT JOIN categories c ON b.categoryId = c.id 
        WHERE b.month = :month AND b.year = :year AND b.categoryId IS NULL
        LIMIT 1
    """)
    fun getOverallBudgetForMonthYear(month: Int, year: Int): Flow<BudgetWithCategory?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND ((:categoryId IS NULL AND categoryId IS NULL) OR categoryId = :categoryId) LIMIT 1")
    suspend fun findBudget(month: Int, year: Int, categoryId: Long?): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}
