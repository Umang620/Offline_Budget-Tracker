package com.example.magtipidka.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import com.example.magtipidka.domain.usecase.budget.BudgetProgress
import com.example.magtipidka.domain.usecase.budget.CalculateBudgetProgressUseCase
import com.example.magtipidka.domain.usecase.budget.DeleteBudgetUseCase
import com.example.magtipidka.domain.usecase.budget.SetBudgetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

class BudgetViewModel(
    private val setBudgetUseCase: SetBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val _month = MutableStateFlow(cal.get(Calendar.MONTH) + 1)
    private val _year = MutableStateFlow(cal.get(Calendar.YEAR))
    private val _selectedPeriod = MutableStateFlow(BudgetPeriod.MONTHLY)

    private val _isAddEditBudgetDialogOpen = MutableStateFlow(false)
    private val _editingBudgetProgress = MutableStateFlow<BudgetProgress?>(null)
    private val _isCategoryBudgetMode = MutableStateFlow(false)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _inputAmount = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        observeBudgetData()
    }

    private fun observeBudgetData() {
        val dateAndPeriodFlow = combine(_month, _year, _selectedPeriod) { m, y, p -> Triple(m, y, p) }

        val monthlyTransactionsFlow = transactionRepository.getTransactionsByMonthYear(_month.value, _year.value)

        val budgetProgressFlow = combine(
            calculateBudgetProgressUseCase.forOverall(_month.value, _year.value),
            calculateBudgetProgressUseCase.forCategoryBudgets(_month.value, _year.value),
            monthlyTransactionsFlow,
            _selectedPeriod
        ) { overallMonthly, categoryMonthlyBudgets, transactions, period ->
            val nowCal = Calendar.getInstance()
            val todayDay = nowCal.get(Calendar.DAY_OF_MONTH)
            val currentWeek = nowCal.get(Calendar.WEEK_OF_YEAR)
            val daysInMonth = nowCal.getActualMaximum(Calendar.DAY_OF_MONTH).toDouble().coerceAtLeast(1.0)

            val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

            val todayExpenses = expenseTransactions.filter { t ->
                val c = Calendar.getInstance().apply { timeInMillis = t.date }
                c.get(Calendar.DAY_OF_MONTH) == todayDay
            }

            val weekExpenses = expenseTransactions.filter { t ->
                val c = Calendar.getInstance().apply { timeInMillis = t.date }
                c.get(Calendar.WEEK_OF_YEAR) == currentWeek
            }

            // Scale Overall Budget
            val scaledOverall = if (overallMonthly != null) {
                val targetAmount = when (period) {
                    BudgetPeriod.MONTHLY -> overallMonthly.budget.amount
                    BudgetPeriod.WEEKLY -> overallMonthly.budget.amount / 4.0
                    BudgetPeriod.DAILY -> overallMonthly.budget.amount / daysInMonth
                }

                val spent = when (period) {
                    BudgetPeriod.MONTHLY -> overallMonthly.spentAmount
                    BudgetPeriod.WEEKLY -> weekExpenses.sumOf { it.amount }
                    BudgetPeriod.DAILY -> todayExpenses.sumOf { it.amount }
                }

                val remaining = targetAmount - spent
                val progress = if (targetAmount > 0) (spent / targetAmount).toFloat() else 0f
                BudgetProgress(
                    budget = overallMonthly.budget.copy(amount = targetAmount),
                    spentAmount = spent,
                    remainingAmount = remaining,
                    progressPercentage = progress,
                    isExceeded = spent > targetAmount
                )
            } else null

            // Scale Category Budgets
            val scaledCategories = categoryMonthlyBudgets.map { catProgress ->
                val catId = catProgress.budget.categoryId
                val targetAmount = when (period) {
                    BudgetPeriod.MONTHLY -> catProgress.budget.amount
                    BudgetPeriod.WEEKLY -> catProgress.budget.amount / 4.0
                    BudgetPeriod.DAILY -> catProgress.budget.amount / daysInMonth
                }

                val spent = when (period) {
                    BudgetPeriod.MONTHLY -> catProgress.spentAmount
                    BudgetPeriod.WEEKLY -> weekExpenses.filter { it.categoryId == catId }.sumOf { it.amount }
                    BudgetPeriod.DAILY -> todayExpenses.filter { it.categoryId == catId }.sumOf { it.amount }
                }

                val remaining = targetAmount - spent
                val progress = if (targetAmount > 0) (spent / targetAmount).toFloat() else 0f
                BudgetProgress(
                    budget = catProgress.budget.copy(amount = targetAmount),
                    spentAmount = spent,
                    remainingAmount = remaining,
                    progressPercentage = progress,
                    isExceeded = spent > targetAmount
                )
            }

            Pair(scaledOverall, scaledCategories)
        }

        val metaFlow = combine(
            categoryRepository.getCategoriesByType(CategoryType.EXPENSE),
            settingsRepository.getSettings()
        ) { categories, settings ->
            Pair(categories, settings)
        }

        val dialogStateFlow = combine(
            _isAddEditBudgetDialogOpen,
            _editingBudgetProgress,
            _isCategoryBudgetMode,
            _selectedCategoryId,
            _inputAmount
        ) { open, editing, isCatMode, catId, inputAmt ->
            DialogState(open, editing, isCatMode, catId, inputAmt)
        }

        combine(
            dateAndPeriodFlow,
            budgetProgressFlow,
            metaFlow,
            dialogStateFlow,
            _errorMessage
        ) { dateAndPeriod, budget, meta, dialog, error ->
            val m = dateAndPeriod.first
            val y = dateAndPeriod.second
            val p = dateAndPeriod.third
            val monthNames = DateFormatSymbols(Locale.ENGLISH).months
            val mName = "${monthNames.getOrElse(m - 1) { "" }} $y"

            BudgetUiState(
                isLoading = false,
                month = m,
                year = y,
                monthName = mName,
                selectedPeriod = p,
                currencySymbol = meta.second.currencySymbol,
                overallBudgetProgress = budget.first,
                categoryBudgetProgresses = budget.second,
                expenseCategories = meta.first,
                isAddEditBudgetDialogOpen = dialog.open,
                editingBudgetProgress = dialog.editing,
                isCategoryBudgetMode = dialog.isCatMode,
                selectedCategoryId = dialog.catId,
                inputAmount = dialog.inputAmt,
                errorMessage = error
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private data class DialogState(
        val open: Boolean,
        val editing: BudgetProgress?,
        val isCatMode: Boolean,
        val catId: Long?,
        val inputAmt: String
    )

    fun onBudgetPeriodSelected(period: BudgetPeriod) {
        _selectedPeriod.value = period
    }

    fun onOpenAddOverallBudgetDialog() {
        val currentOverall = _uiState.value.overallBudgetProgress
        _editingBudgetProgress.value = currentOverall
        _isCategoryBudgetMode.value = false
        _selectedCategoryId.value = null
        _inputAmount.value = currentOverall?.budget?.amount?.toString() ?: ""
        _errorMessage.value = null
        _isAddEditBudgetDialogOpen.value = true
    }

    fun onOpenAddCategoryBudgetDialog(existingProgress: BudgetProgress? = null) {
        _editingBudgetProgress.value = existingProgress
        _isCategoryBudgetMode.value = true
        _selectedCategoryId.value = existingProgress?.budget?.categoryId ?: _uiState.value.expenseCategories.firstOrNull()?.id
        _inputAmount.value = existingProgress?.budget?.amount?.toString() ?: ""
        _errorMessage.value = null
        _isAddEditBudgetDialogOpen.value = true
    }

    fun onDismissBudgetDialog() {
        _isAddEditBudgetDialogOpen.value = false
    }

    fun onInputAmountChanged(amount: String) {
        _inputAmount.value = amount
        _errorMessage.value = null
    }

    fun onCategorySelected(categoryId: Long) {
        _selectedCategoryId.value = categoryId
    }

    fun onSaveBudget() {
        val amountVal = _inputAmount.value.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            _errorMessage.value = "Please enter a valid amount greater than zero."
            return
        }

        val isCatMode = _isCategoryBudgetMode.value
        val catId = if (isCatMode) _selectedCategoryId.value else null

        if (isCatMode && (catId == null || catId <= 0)) {
            _errorMessage.value = "Please select a category."
            return
        }

        val period = _selectedPeriod.value
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH).toDouble().coerceAtLeast(1.0)

        val monthlyAmount = when (period) {
            BudgetPeriod.MONTHLY -> amountVal
            BudgetPeriod.WEEKLY -> amountVal * 4.0
            BudgetPeriod.DAILY -> amountVal * daysInMonth
        }

        val budget = Budget(
            id = _editingBudgetProgress.value?.budget?.id ?: 0L,
            categoryId = catId,
            amount = monthlyAmount,
            month = _month.value,
            year = _year.value
        )

        viewModelScope.launch {
            try {
                setBudgetUseCase(budget)
                _isAddEditBudgetDialogOpen.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save budget."
            }
        }
    }

    fun onDeleteBudget(budget: Budget) {
        viewModelScope.launch {
            deleteBudgetUseCase(budget)
        }
    }

    class Factory(
        private val setBudgetUseCase: SetBudgetUseCase,
        private val deleteBudgetUseCase: DeleteBudgetUseCase,
        private val calculateBudgetProgressUseCase: CalculateBudgetProgressUseCase,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BudgetViewModel(
                setBudgetUseCase,
                deleteBudgetUseCase,
                calculateBudgetProgressUseCase,
                categoryRepository,
                settingsRepository,
                transactionRepository
            ) as T
        }
    }
}
