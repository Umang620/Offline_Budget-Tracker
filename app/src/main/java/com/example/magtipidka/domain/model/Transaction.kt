package com.example.magtipidka.domain.model

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val receiptImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
