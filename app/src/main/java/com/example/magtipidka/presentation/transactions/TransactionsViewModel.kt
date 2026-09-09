package com.example.magtipidka.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.magtipidka.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow(CategoryType.ALL)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)
    private val _transactionToDelete = MutableStateFlow<Transaction?>(null)

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        val filterParamsFlow = combine(
            _searchQuery,
            _selectedType,
            _selectedCategoryId,
            _startDate,
            _endDate
        ) { query, type, catId, sDate, eDate ->
            FilterParams(query, type, catId, sDate, eDate)
        }

        val filteredTransactionsFlow = filterParamsFlow.flatMapLatest { params ->
            val typeEnum = when (params.type) {
                CategoryType.INCOME -> TransactionType.INCOME
                CategoryType.EXPENSE -> TransactionType.EXPENSE
                CategoryType.ALL -> null
            }
            if (params.query.isNotBlank()) {
                getTransactionsUseCase.search(params.query)
            } else {
                getTransactionsUseCase.filter(
                    type = typeEnum,
                    categoryId = params.categoryId,
                    startDate = params.startDate,
                    endDate = params.endDate
                )
            }
        }

        val metaDataFlow = combine(
            categoryRepository.getAllCategories(),
            settingsRepository.getSettings(),
            _transactionToDelete
        ) { categories, settings, toDelete ->
            Triple(categories, settings, toDelete)
        }

        combine(filteredTransactionsFlow, metaDataFlow, filterParamsFlow) { transactions, meta, params ->
            val categories = meta.first
            val settings = meta.second
            val toDelete = meta.third

            TransactionsUiState(
                isLoading = false,
                searchQuery = params.query,
                selectedType = params.type,
                selectedCategoryId = params.categoryId,
                startDate = params.startDate,
                endDate = params.endDate,
                currencySymbol = settings.currencySymbol,
                transactions = transactions,
                categories = categories,
                transactionToDelete = toDelete
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTypeSelected(type: CategoryType) {
        _selectedType.value = type
        _selectedCategoryId.value = null
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun onRequestDeleteTransaction(transaction: Transaction) {
        _transactionToDelete.value = transaction
    }

    fun onDismissDeleteDialog() {
        _transactionToDelete.value = null
    }

    fun onConfirmDeleteTransaction() {
        val transaction = _transactionToDelete.value ?: return
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
            _transactionToDelete.value = null
        }
    }

    private data class FilterParams(
        val query: String,
        val type: CategoryType,
        val categoryId: Long?,
        val startDate: Long?,
        val endDate: Long?
    )

    class Factory(
        private val getTransactionsUseCase: GetTransactionsUseCase,
        private val deleteTransactionUseCase: DeleteTransactionUseCase,
        private val categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(
                getTransactionsUseCase,
                deleteTransactionUseCase,
                categoryRepository,
                settingsRepository
            ) as T
        }
    }
}
