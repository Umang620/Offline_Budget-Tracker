package com.example.magtipidka.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.engine.CategoryKeywordEngine
import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.usecase.transaction.AddTransactionUseCase
import com.example.magtipidka.domain.usecase.transaction.EditTransactionUseCase
import com.example.magtipidka.domain.usecase.transaction.GetTransactionByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEditTransactionViewModel(
    private val transactionId: Long,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val editTransactionUseCase: EditTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTransactionUiState(transactionId = transactionId, isEditMode = transactionId > 0))
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings().first()
            _uiState.value = _uiState.value.copy(currencySymbol = settings.currencySymbol)

            observeCategories()

            if (transactionId > 0) {
                val existing = getTransactionByIdUseCase(transactionId)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(
                        type = existing.type,
                        amount = existing.amount.toString(),
                        selectedCategoryId = existing.categoryId,
                        description = existing.description,
                        date = existing.date,
                        receiptImageUri = existing.receiptImageUri
                    )
                }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            val type = _uiState.value.type
            val categoryType = if (type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            categoryRepository.getCategoriesByType(categoryType).collect { list ->
                val currentSelected = _uiState.value.selectedCategoryId
                val defaultSelect = if (currentSelected == null || list.none { it.id == currentSelected }) {
                    list.firstOrNull()?.id
                } else currentSelected

                _uiState.value = _uiState.value.copy(
                    categories = list,
                    selectedCategoryId = defaultSelect
                )
            }
        }
    }

    fun onTypeChanged(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type, selectedCategoryId = null)
        observeCategories()
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount, errorMessage = null)
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId, errorMessage = null)
    }

    fun onDescriptionChanged(description: String) {
        val matchedCategory = CategoryKeywordEngine.findMatchingCategory(
            description,
            _uiState.value.categories
        )
        val newCatId = matchedCategory?.id ?: _uiState.value.selectedCategoryId

        _uiState.value = _uiState.value.copy(
            description = description,
            selectedCategoryId = newCatId
        )
    }

    fun onDateChanged(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onReceiptImageSelected(uri: String?) {
        _uiState.value = _uiState.value.copy(receiptImageUri = uri)
    }

    fun onShowAddCustomCategoryDialog() {
        _uiState.value = _uiState.value.copy(isAddingCustomCategory = true, newCustomCategoryName = "")
    }

    fun onDismissCustomCategoryDialog() {
        _uiState.value = _uiState.value.copy(isAddingCustomCategory = false, newCustomCategoryName = "")
    }

    fun onCustomCategoryNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(newCustomCategoryName = name)
    }

    fun onSaveCustomCategory() {
        val name = _uiState.value.newCustomCategoryName.trim()
        if (name.isEmpty()) return

        val catType = if (_uiState.value.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        viewModelScope.launch {
            val newCategory = Category(
                name = name,
                type = catType,
                isDefault = false,
                iconName = "category"
            )
            val id = categoryRepository.insertCategory(newCategory)
            _uiState.value = _uiState.value.copy(
                selectedCategoryId = id,
                isAddingCustomCategory = false,
                newCustomCategoryName = ""
            )
        }
    }

    fun onSaveTransaction() {
        val amountVal = _uiState.value.amount.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid amount greater than zero.")
            return
        }

        val categoryId = _uiState.value.selectedCategoryId
        if (categoryId == null || categoryId <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please select a category.")
            return
        }

        val state = _uiState.value
        val transaction = Transaction(
            id = if (state.isEditMode) state.transactionId else 0L,
            type = state.type,
            amount = amountVal,
            categoryId = categoryId,
            description = state.description.trim(),
            date = state.date,
            receiptImageUri = state.receiptImageUri
        )

        viewModelScope.launch {
            try {
                if (state.isEditMode) {
                    editTransactionUseCase(transaction)
                } else {
                    addTransactionUseCase(transaction)
                }
                _uiState.value = _uiState.value.copy(isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to save transaction.")
            }
        }
    }

    class Factory(
        private val transactionId: Long,
        private val addTransactionUseCase: AddTransactionUseCase,
        private val editTransactionUseCase: EditTransactionUseCase,
        private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditTransactionViewModel(
                transactionId,
                addTransactionUseCase,
                editTransactionUseCase,
                getTransactionByIdUseCase,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}
