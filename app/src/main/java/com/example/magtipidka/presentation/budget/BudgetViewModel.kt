package com.example.magtipidka.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.SettingsRepository
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
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val _month = MutableStateFlow(cal.get(Calendar.MONTH) + 1)
    private val _year = MutableStateFlow(cal.get(Calendar.YEAR))

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
        val dateFlow = combine(_month, _year) { m, y -> Pair(m, y) }

        val budgetProgressFlow = combine(
            calculateBudgetProgressUseCase.forOverall(_month.value, _year.value),
            calculateBudgetProgressUseCase.forCategoryBudgets(_month.value, _year.value)
        ) { overall, categoryBudgets ->
            Pair(overall, categoryBudgets)
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
            dateFlow,
            budgetProgressFlow,
            metaFlow,
            dialogStateFlow,
            _errorMessage
        ) { date, budget, meta, dialog, error ->
            val m = date.first
            val y = date.second
            val monthNames = DateFormatSymbols(Locale.ENGLISH).months
            val mName = "${monthNames.getOrElse(m - 1) { "" }} $y"

            BudgetUiState(
                isLoading = false,
                month = m,
                year = y,
                monthName = mName,
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

        val budget = Budget(
            id = _editingBudgetProgress.value?.budget?.id ?: 0L,
            categoryId = catId,
            amount = amountVal,
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
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BudgetViewModel(
                setBudgetUseCase,
                deleteBudgetUseCase,
                calculateBudgetProgressUseCase,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}
