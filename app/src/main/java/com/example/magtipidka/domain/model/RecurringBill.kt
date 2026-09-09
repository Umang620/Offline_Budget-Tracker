package com.example.magtipidka.domain.model

enum class RecurringFrequency {
    MONTHLY,
    WEEKLY
}

data class RecurringBill(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String? = null,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDate: Long,
    val isAutoAdd: Boolean = true,
    val lastProcessedDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
