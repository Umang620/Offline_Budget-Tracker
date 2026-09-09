package com.example.magtipidka.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.model.RecurringFrequency
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.RecurringBillRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RecurringBillsViewModel(
    private val recurringBillRepository: RecurringBillRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isAddEditBillDialogOpen = MutableStateFlow(false)
    private val _editingBill = MutableStateFlow<RecurringBill?>(null)
    private val _titleInput = MutableStateFlow("")
    private val _amountInput = MutableStateFlow("")
    private val _categoryIdInput = MutableStateFlow<Long?>(null)
    private val _frequencyInput = MutableStateFlow(RecurringFrequency.MONTHLY)
    private val _nextDueDateInput = MutableStateFlow(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(RecurringBillsUiState())
    val uiState: StateFlow<RecurringBillsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val fieldsFlow = combine(
            _titleInput,
            _amountInput,
            _categoryIdInput,
            _frequencyInput,
            _nextDueDateInput
        ) { title, amount, catId, freq, date ->
            BillFields(title, amount, catId, freq, date)
        }

        val dialogStateFlow = combine(
            _isAddEditBillDialogOpen,
            _editingBill,
            fieldsFlow
        ) { open, editing, fields ->
            BillDialogState(open, editing, fields.title, fields.amount, fields.catId, fields.freq, fields.date)
        }

        val metaFlow = combine(
            recurringBillRepository.getAllRecurringBills(),
            categoryRepository.getCategoriesByType(CategoryType.EXPENSE),
            settingsRepository.getSettings()
        ) { bills, categories, settings ->
            Triple(bills, categories, settings.currencySymbol)
        }

        combine(
            metaFlow,
            dialogStateFlow,
            _errorMessage
        ) { meta, dialogState, error ->
            RecurringBillsUiState(
                isLoading = false,
                currencySymbol = meta.third,
                recurringBills = meta.first,
                expenseCategories = meta.second,
                isAddEditBillDialogOpen = dialogState.open,
                editingBill = dialogState.editing,
                titleInput = dialogState.title,
                amountInput = dialogState.amount,
                categoryIdInput = dialogState.catId ?: meta.second.firstOrNull()?.id,
                frequencyInput = dialogState.freq,
                nextDueDateInput = dialogState.date,
                errorMessage = error
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private data class BillFields(
        val title: String,
        val amount: String,
        val catId: Long?,
        val freq: RecurringFrequency,
        val date: Long
    )

    private data class BillDialogState(
        val open: Boolean,
        val editing: RecurringBill?,
        val title: String,
        val amount: String,
        val catId: Long?,
        val freq: RecurringFrequency,
        val date: Long
    )

    fun onOpenAddBillDialog(existingBill: RecurringBill? = null) {
        _editingBill.value = existingBill
        _titleInput.value = existingBill?.title ?: ""
        _amountInput.value = existingBill?.amount?.toString() ?: ""
        _categoryIdInput.value = existingBill?.categoryId ?: _uiState.value.expenseCategories.firstOrNull()?.id
        _frequencyInput.value = existingBill?.frequency ?: RecurringFrequency.MONTHLY
        _nextDueDateInput.value = existingBill?.nextDueDate ?: (System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
        _errorMessage.value = null
        _isAddEditBillDialogOpen.value = true
    }

    fun onDismissBillDialog() {
        _isAddEditBillDialogOpen.value = false
    }

    fun onTitleChanged(title: String) { _titleInput.value = title; _errorMessage.value = null }
    fun onAmountChanged(amount: String) { _amountInput.value = amount; _errorMessage.value = null }
    fun onCategorySelected(categoryId: Long) { _categoryIdInput.value = categoryId }
    fun onFrequencySelected(frequency: RecurringFrequency) { _frequencyInput.value = frequency }
    fun onNextDueDateChanged(date: Long) { _nextDueDateInput.value = date }

    fun onSaveBill() {
        val title = _titleInput.value.trim()
        if (title.isEmpty()) { _errorMessage.value = "Please enter a bill title."; return }

        val amountVal = _amountInput.value.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) { _errorMessage.value = "Please enter a valid amount."; return }

        val categoryId = _categoryIdInput.value ?: _uiState.value.expenseCategories.firstOrNull()?.id ?: 0L

        val bill = RecurringBill(
            id = _editingBill.value?.id ?: 0L,
            title = title,
            amount = amountVal,
            categoryId = categoryId,
            frequency = _frequencyInput.value,
            nextDueDate = _nextDueDateInput.value
        )

        viewModelScope.launch {
            try {
                if (_editingBill.value != null) {
                    recurringBillRepository.updateRecurringBill(bill)
                } else {
                    recurringBillRepository.insertRecurringBill(bill)
                }
                _isAddEditBillDialogOpen.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save bill."
            }
        }
    }

    fun onDeleteBill(bill: RecurringBill) {
        viewModelScope.launch {
            recurringBillRepository.deleteRecurringBill(bill)
        }
    }

    class Factory(
        private val recurringBillRepository: RecurringBillRepository,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecurringBillsViewModel(
                recurringBillRepository,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}
