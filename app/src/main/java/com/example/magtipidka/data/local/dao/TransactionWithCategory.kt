package com.example.magtipidka.data.local.dao

import androidx.room.Embedded
import com.example.magtipidka.data.local.entity.TransactionEntity

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    val categoryName: String?
)

data class CategoryExpenseSum(
    val categoryId: Long,
    val totalAmount: Double
)
