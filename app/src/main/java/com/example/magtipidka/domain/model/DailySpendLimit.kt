package com.example.magtipidka.domain.model

enum class DailyLimitStatus {
    SAFE,
    WARNING,
    EXCEEDED
}

data class DailySpendLimit(
    val safeDailyLimit: Double,
    val remainingBudget: Double,
    val daysRemainingInMonth: Int,
    val status: DailyLimitStatus
)
