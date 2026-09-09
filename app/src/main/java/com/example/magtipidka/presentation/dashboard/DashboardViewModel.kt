package com.example.magtipidka.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.CategoryExpenseShare
import com.example.magtipidka.domain.model.DailySpendLimit
import com.example.magtipidka.domain.model.DebtType
import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.model.TipidScore
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.DebtRepository
import com.example.magtipidka.domain.repository.RecurringBillRepository
import com.example.magtipidka.domain.repository.SavingsGoalRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.usecase.budget.CalculateBudgetProgressUseCase
import com.example.magtipidka.domain.usecase.budget.CalculateDailySpendLimitUseCase
import com.example.magtipidka.domain.usecase.budget.CalculateTipidScoreUseCase
import com.example.magtipidka.domain.usecase.recurring.ProcessDueRecurringBillsUseCase
import com.example.magtipidka.domain.usecase.transaction.CalculateBalanceUseCase
import com.example.magtipidka.domain.usecase.transaction.CalculateTotalExpensesUseCase
import com.example.magtipidka.domain.usecase.transaction.CalculateTotalIncomeUseCase
import com.example.magtipidka.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val calculateTotalIncomeUseCase: CalculateTotalIncomeUseCase,
    private val calculateTotalExpensesUseCase: CalculateTotalExpensesUseCase,
    private val calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase,
    private val calculateDailySpendLimitUseCase: CalculateDailySpendLimitUseCase,
    private val calculateTipidScoreUseCase: CalculateTipidScoreUseCase,
    private val processDueRecurringBillsUseCase: ProcessDueRecurringBillsUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val debtRepository: DebtRepository,
    private val recurringBillRepository: RecurringBillRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _dueBillsForApproval = MutableStateFlow<List<RecurringBill>>(emptyList())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.insertDefaultCategoriesIfNeeded()
            checkDueRecurringBills()
        }
        observeDashboardData()
    }

    private fun checkDueRecurringBills() {
        viewModelScope.launch {
            try {
                val due = processDueRecurringBillsUseCase.getDueBillsForApproval()
                _dueBillsForApproval.value = due
            } catch (e: Exception) {
                _dueBillsForApproval.value = emptyList()
            }
        }
    }

    fun onApproveAndPostBill(bill: RecurringBill) {
        viewModelScope.launch {
            try {
                processDueRecurringBillsUseCase.approveAndPostBill(bill)
                checkDueRecurringBills()
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    fun onSkipBill(bill: RecurringBill) {
        _dueBillsForApproval.value = _dueBillsForApproval.value.filter { it.id != bill.id }
    }

    private fun observeDashboardData() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        val flow1 = combine(
            settingsRepository.getSettings(),
            calculateBalanceUseCase(),
            calculateTotalIncomeUseCase.byMonthYear(currentMonth, currentYear),
            calculateTotalExpensesUseCase.byMonthYear(currentMonth, currentYear),
            calculateBudgetProgressUseCase.forOverall(currentMonth, currentYear)
        ) { settings, balance, monthlyIncome, monthlyExpenses, budgetProgress ->
            Quintet(settings, balance, monthlyIncome, monthlyExpenses, budgetProgress)
        }

        val flow2 = combine(
            calculateTotalIncomeUseCase(),
            calculateTotalExpensesUseCase(),
            calculateDailySpendLimitUseCase(),
            calculateTipidScoreUseCase(),
            getTransactionsUseCase()
        ) { allTimeIncome, allTimeExpenses, dailyLimit, score, transactions ->
            AllTimeData(allTimeIncome, allTimeExpenses, dailyLimit, score, transactions)
        }

        val flow3 = combine(
            debtRepository.getAllDebts(),
            recurringBillRepository.getAllRecurringBills(),
            savingsGoalRepository.getAllSavingsGoals(),
            _dueBillsForApproval
        ) { debts, bills, goals, dueBills ->
            val utang = debts.filter { !it.isSettled && it.type == DebtType.IOWE }.sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
            val pautang = debts.filter { !it.isSettled && it.type == DebtType.OWED_TO_ME }.sumOf { (it.amount - it.paidAmount).coerceAtLeast(0.0) }
            val billsToPay = bills.sumOf { it.amount }
            FinancialCommitments(utang, pautang, billsToPay, goals, dueBills)
        }

        combine(flow1, flow2, flow3) { f1, f2, f3 ->
            val settings = f1.settings
            val balance = f1.balance
            val monthlyIncome = f1.income
            val monthlyExpenses = f1.expenses
            val budgetProgress = f1.budgetProgress

            val allTimeIncome = f2.allTimeIncome
            val allTimeExpenses = f2.allTimeExpenses
            val dailyLimit = f2.dailyLimit
            val tipidScore = f2.score
            val transactions = f2.transactions

            val utang = f3.utang
            val pautang = f3.pautang
            val billsToPay = f3.billsToPay
            val goals = f3.goals
            val dueBills = f3.dueBills

            val recent = transactions.take(5)

            // Filter transactions specifically for the current month
            val currentMonthTransactions = transactions.filter { t ->
                val tCal = Calendar.getInstance().apply { timeInMillis = t.date }
                tCal.get(Calendar.MONTH) + 1 == currentMonth && tCal.get(Calendar.YEAR) == currentYear
            }

            val totalExpense = if (monthlyExpenses > 0) monthlyExpenses else 1.0
            val categoryExpenses = currentMonthTransactions
                .filter { it.type.name == "EXPENSE" }
                .groupBy { it.categoryId }
                .map { (catId, catTransactions) ->
                    val sum = catTransactions.sumOf { it.amount }
                    val catName = catTransactions.firstOrNull()?.categoryName ?: "Uncategorized"
                    val pct = ((sum / totalExpense) * 100).toFloat()
                    CategoryExpenseShare(
                        categoryId = catId,
                        categoryName = catName,
                        amount = sum,
                        percentage = pct
                    )
                }.sortedByDescending { it.amount }

            val topExpenseCategory = categoryExpenses.firstOrNull()

            DashboardUiState(
                isLoading = false,
                currencySymbol = settings.currencySymbol,
                currentBalance = balance,
                totalIncome = monthlyIncome,
                totalExpenses = monthlyExpenses,
                allTimeIncome = allTimeIncome,
                allTimeExpenses = allTimeExpenses,
                totalUtang = utang,
                totalPautang = pautang,
                totalBillsToPay = billsToPay,
                topExpenseCategoryShare = topExpenseCategory,
                tipidScore = tipidScore,
                dueRecurringBillsForApproval = dueBills,
                overallBudgetProgress = budgetProgress,
                dailySpendLimit = dailyLimit,
                recentTransactions = recent,
                topCategoryExpenses = categoryExpenses.take(4),
                savingsGoals = goals.take(3)
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private data class Quintet<A, B, C, D, E>(
        val settings: A,
        val balance: B,
        val income: C,
        val expenses: D,
        val budgetProgress: E
    )

    private data class AllTimeData(
        val allTimeIncome: Double,
        val allTimeExpenses: Double,
        val dailyLimit: DailySpendLimit?,
        val score: TipidScore,
        val transactions: List<Transaction>
    )

    private data class FinancialCommitments(
        val utang: Double,
        val pautang: Double,
        val billsToPay: Double,
        val goals: List<SavingsGoal>,
        val dueBills: List<RecurringBill>
    )

    class Factory(
        private val calculateBalanceUseCase: CalculateBalanceUseCase,
        private val calculateTotalIncomeUseCase: CalculateTotalIncomeUseCase,
        private val calculateTotalExpensesUseCase: CalculateTotalExpensesUseCase,
        private val calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase,
        private val calculateDailySpendLimitUseCase: CalculateDailySpendLimitUseCase,
        private val calculateTipidScoreUseCase: CalculateTipidScoreUseCase,
        private val processDueRecurringBillsUseCase: ProcessDueRecurringBillsUseCase,
        private val getTransactionsUseCase: GetTransactionsUseCase,
        private val savingsGoalRepository: SavingsGoalRepository,
        private val debtRepository: DebtRepository,
        private val recurringBillRepository: RecurringBillRepository,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(
                calculateBalanceUseCase,
                calculateTotalIncomeUseCase,
                calculateTotalExpensesUseCase,
                calculateBudgetProgressUseCase,
                calculateDailySpendLimitUseCase,
                calculateTipidScoreUseCase,
                processDueRecurringBillsUseCase,
                getTransactionsUseCase,
                savingsGoalRepository,
                debtRepository,
                recurringBillRepository,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}
