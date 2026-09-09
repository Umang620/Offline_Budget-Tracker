package com.example.magtipidka.presentation.transactions

import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.model.Transaction

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedType: CategoryType = CategoryType.ALL,
    val selectedCategoryId: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val currencySymbol: String = "₱",
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactionToDelete: Transaction? = null
)
