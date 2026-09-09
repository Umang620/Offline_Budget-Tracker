package com.example.magtipidka.di

import android.content.Context
import com.example.magtipidka.data.local.database.AppDatabase
import com.example.magtipidka.data.local.datastore.SettingsDataStore
import com.example.magtipidka.data.repository.BudgetRepositoryImpl
import com.example.magtipidka.data.repository.CategoryRepositoryImpl
import com.example.magtipidka.data.repository.DataBackupRepositoryImpl
import com.example.magtipidka.data.repository.DebtRepositoryImpl
import com.example.magtipidka.data.repository.RecurringBillRepositoryImpl
import com.example.magtipidka.data.repository.SavingsGoalRepositoryImpl
import com.example.magtipidka.data.repository.SettingsRepositoryImpl
import com.example.magtipidka.data.repository.TransactionRepositoryImpl
import com.example.magtipidka.domain.repository.BudgetRepository
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.DataBackupRepository
import com.example.magtipidka.domain.repository.DebtRepository
import com.example.magtipidka.domain.repository.RecurringBillRepository
import com.example.magtipidka.domain.repository.SavingsGoalRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import com.example.magtipidka.domain.usecase.budget.CalculateBudgetProgressUseCase
import com.example.magtipidka.domain.usecase.budget.CalculateDailySpendLimitUseCase
import com.example.magtipidka.domain.usecase.budget.CalculateTipidScoreUseCase
import com.example.magtipidka.domain.usecase.budget.DeleteBudgetUseCase
import com.example.magtipidka.domain.usecase.budget.GetBudgetUseCase
import com.example.magtipidka.domain.usecase.budget.SetBudgetUseCase
import com.example.magtipidka.domain.usecase.recurring.ProcessDueRecurringBillsUseCase
import com.example.magtipidka.domain.usecase.report.ExportReportCsvPdfUseCase
import com.example.magtipidka.domain.usecase.report.GenerateMonthlyReportUseCase
import com.example.magtipidka.domain.usecase.savings.AddSavingsGoalUseCase
import com.example.magtipidka.domain.usecase.savings.CalculateSavingsProgressUseCase
import com.example.magtipidka.domain.usecase.savings.DeleteSavingsGoalUseCase
import com.example.magtipidka.domain.usecase.savings.UpdateSavingsGoalUseCase
import com.example.magtipidka.domain.usecase.transaction.AddTransactionUseCase
import com.example.magtipidka.domain.usecase.transaction.CalculateBalanceUseCase
import com.example.magtipidka.domain.usecase.transaction.CalculateTotalExpensesUseCase
import com.example.magtipidka.domain.usecase.transaction.CalculateTotalIncomeUseCase
import com.example.magtipidka.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.magtipidka.domain.usecase.transaction.EditTransactionUseCase
import com.example.magtipidka.domain.usecase.transaction.GetTransactionByIdUseCase
import com.example.magtipidka.domain.usecase.transaction.GetTransactionsUseCase

class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepositoryImpl(database.budgetDao())
    }

    val savingsGoalRepository: SavingsGoalRepository by lazy {
        SavingsGoalRepositoryImpl(database.savingsGoalDao())
    }

    val debtRepository: DebtRepository by lazy {
        DebtRepositoryImpl(database.debtDao())
    }

    val recurringBillRepository: RecurringBillRepository by lazy {
        RecurringBillRepositoryImpl(database.recurringBillDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(settingsDataStore)
    }

    val dataBackupRepository: DataBackupRepository by lazy {
        DataBackupRepositoryImpl(database)
    }

    // Transaction Use Cases
    val addTransactionUseCase by lazy { AddTransactionUseCase(transactionRepository) }
    val editTransactionUseCase by lazy { EditTransactionUseCase(transactionRepository) }
    val deleteTransactionUseCase by lazy { DeleteTransactionUseCase(transactionRepository) }
    val getTransactionsUseCase by lazy { GetTransactionsUseCase(transactionRepository) }
    val getTransactionByIdUseCase by lazy { GetTransactionByIdUseCase(transactionRepository) }
    val calculateBalanceUseCase by lazy { CalculateBalanceUseCase(transactionRepository) }
    val calculateTotalIncomeUseCase by lazy { CalculateTotalIncomeUseCase(transactionRepository) }
    val calculateTotalExpensesUseCase by lazy { CalculateTotalExpensesUseCase(transactionRepository) }

    // Budget, Daily Limit & Tipid Score Use Cases
    val setBudgetUseCase by lazy { SetBudgetUseCase(budgetRepository) }
    val getBudgetUseCase by lazy { GetBudgetUseCase(budgetRepository) }
    val deleteBudgetUseCase by lazy { DeleteBudgetUseCase(budgetRepository) }
    val calculateBudgetProgressUseCase by lazy { CalculateBudgetProgressUseCase(budgetRepository, transactionRepository) }
    val calculateDailySpendLimitUseCase by lazy { CalculateDailySpendLimitUseCase(budgetRepository, transactionRepository) }
    val calculateTipidScoreUseCase by lazy { CalculateTipidScoreUseCase(budgetRepository, transactionRepository, savingsGoalRepository, debtRepository) }

    // Recurring Bills Use Cases
    val processDueRecurringBillsUseCase by lazy { ProcessDueRecurringBillsUseCase(recurringBillRepository, transactionRepository) }

    // Savings Goals Use Cases
    val addSavingsGoalUseCase by lazy { AddSavingsGoalUseCase(savingsGoalRepository) }
    val updateSavingsGoalUseCase by lazy { UpdateSavingsGoalUseCase(savingsGoalRepository) }
    val deleteSavingsGoalUseCase by lazy { DeleteSavingsGoalUseCase(savingsGoalRepository) }
    val calculateSavingsProgressUseCase by lazy { CalculateSavingsProgressUseCase() }

    // Report Use Cases
    val generateMonthlyReportUseCase by lazy { GenerateMonthlyReportUseCase(transactionRepository, categoryRepository) }
    val exportReportCsvPdfUseCase by lazy { ExportReportCsvPdfUseCase() }
}
