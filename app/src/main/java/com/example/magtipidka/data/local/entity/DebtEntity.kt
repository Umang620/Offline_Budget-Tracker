package com.example.magtipidka.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val type: String, // "IOWE" (Utang) or "OWED_TO_ME" (Pautang)
    val dueDate: Long,
    val isSettled: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
