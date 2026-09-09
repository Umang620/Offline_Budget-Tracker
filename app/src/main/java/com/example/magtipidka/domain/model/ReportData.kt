package com.example.magtipidka.domain.model

data class CategoryExpenseShare(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class MonthlyTrend(
    val month: Int,
    val year: Int,
    val monthName: String,
    val income: Double,
    val expense: Double
)

data class ReportData(
    val month: Int,
    val year: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val categoryExpenses: List<CategoryExpenseShare>,
    val monthlyTrends: List<MonthlyTrend>
)
