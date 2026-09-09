package com.example.magtipidka.presentation.transactions

import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.model.TransactionType

data class AddEditTransactionUiState(
    val transactionId: Long = 0L,
    val isEditMode: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val receiptImageUri: String? = null,
    val currencySymbol: String = "₱",
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isAddingCustomCategory: Boolean = false,
    val newCustomCategoryName: String = ""
)
