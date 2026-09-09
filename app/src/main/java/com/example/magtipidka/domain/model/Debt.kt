package com.example.magtipidka.domain.model

enum class DebtType {
    IOWE,        // Utang (Money you borrowed)
    OWED_TO_ME   // Pautang (Money friends/others owe you)
}

data class Debt(
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val type: DebtType,
    val dueDate: Long,
    val isSettled: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
