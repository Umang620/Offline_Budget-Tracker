package com.example.magtipidka.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val frequency: String, // "MONTHLY" or "WEEKLY"
    val nextDueDate: Long,
    val isAutoAdd: Boolean = true,
    val lastProcessedDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
