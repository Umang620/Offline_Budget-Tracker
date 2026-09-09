package com.example.magtipidka.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val categoryId: Long,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val receiptImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
