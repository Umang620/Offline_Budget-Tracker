package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.database.AppDatabase
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.BackupData
import com.example.magtipidka.domain.repository.DataBackupRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first

class DataBackupRepositoryImpl(
    private val database: AppDatabase
) : DataBackupRepository {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override suspend fun exportDataJson(): String {
        val categories = database.categoryDao().getAllCategories().first().map { it.toDomain() }
        val transactions = database.transactionDao().getAllTransactions().first().map { it.toDomain() }
        val budgets = database.budgetDao().getAllBudgets().first().map { it.toDomain() }
        val savingsGoals = database.savingsGoalDao().getAllSavingsGoals().first().map { it.toDomain() }
        val debts = database.debtDao().getAllDebts().first().map { it.toDomain() }
        val recurringBills = database.recurringBillDao().getAllRecurringBills().first().map { it.toDomain() }

        val backupData = BackupData(
            version = 2,
            exportDate = System.currentTimeMillis(),
            categories = categories,
            transactions = transactions,
            budgets = budgets,
            savingsGoals = savingsGoals,
            debts = debts,
            recurringBills = recurringBills
        )

        return gson.toJson(backupData)
    }

    override suspend fun importDataJson(jsonString: String): Boolean {
        return try {
            val backupData = gson.fromJson(jsonString, BackupData::class.java) ?: return false

            if (backupData.categories.isNotEmpty()) {
                val categoryEntities = backupData.categories.map { it.toEntity() }
                database.categoryDao().insertCategories(categoryEntities)
            }

            if (backupData.transactions.isNotEmpty()) {
                val transactionEntities = backupData.transactions.map { it.toEntity() }
                database.transactionDao().insertTransactions(transactionEntities)
            }

            if (backupData.budgets.isNotEmpty()) {
                val budgetEntities = backupData.budgets.map { it.toEntity() }
                database.budgetDao().insertBudgets(budgetEntities)
            }

            if (backupData.savingsGoals.isNotEmpty()) {
                val savingsGoalEntities = backupData.savingsGoals.map { it.toEntity() }
                database.savingsGoalDao().insertSavingsGoals(savingsGoalEntities)
            }

            if (backupData.debts.isNotEmpty()) {
                val debtEntities = backupData.debts.map { it.toEntity() }
                debtEntities.forEach { database.debtDao().insertDebt(it) }
            }

            if (backupData.recurringBills.isNotEmpty()) {
                val billEntities = backupData.recurringBills.map { it.toEntity() }
                billEntities.forEach { database.recurringBillDao().insertRecurringBill(it) }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
