package com.example.magtipidka.domain.usecase.report

import com.example.magtipidka.domain.model.CategoryExpenseShare
import com.example.magtipidka.domain.model.MonthlyTrend
import com.example.magtipidka.domain.model.ReportData
import com.example.magtipidka.domain.repository.CategoryRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

class GenerateMonthlyReportUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(month: Int, year: Int): Flow<ReportData> {
        val incomeFlow = transactionRepository.getTotalIncomeByMonthYear(month, year)
        val expensesFlow = transactionRepository.getTotalExpensesByMonthYear(month, year)
        val categoryExpensesFlow = transactionRepository.getCategoryExpensesByMonthYear(month, year)
        val categoriesFlow = categoryRepository.getAllCategories()

        return combine(
            incomeFlow,
            expensesFlow,
            categoryExpensesFlow,
            categoriesFlow
        ) { income, expense, categoryExpenses, categories ->
            val net = income - expense
            val categoryMap = categories.associateBy { it.id }

            val totalExpenseForPercentages = if (expense > 0) expense else 1.0
            val expenseShares = categoryExpenses.map { (catId, amount) ->
                val catName = categoryMap[catId]?.name ?: "Uncategorized"
                val pct = ((amount / totalExpenseForPercentages) * 100).toFloat()
                CategoryExpenseShare(
                    categoryId = catId,
                    categoryName = catName,
                    amount = amount,
                    percentage = pct
                )
            }.sortedByDescending { it.amount }

            // Build past 6 months trends asynchronously
            val trends = mutableListOf<MonthlyTrend>()
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)

            val monthNames = DateFormatSymbols(Locale.ENGLISH).shortMonths

            for (i in 5 downTo 0) {
                val trendCal = cal.clone() as Calendar
                trendCal.add(Calendar.MONTH, -i)
                val tMonth = trendCal.get(Calendar.MONTH) + 1
                val tYear = trendCal.get(Calendar.YEAR)
                val mName = monthNames.getOrElse(tMonth - 1) { "$tMonth" }

                // We can insert placeholder or collect inside flow if needed
                trends.add(
                    MonthlyTrend(
                        month = tMonth,
                        year = tYear,
                        monthName = mName,
                        income = 0.0,
                        expense = 0.0
                    )
                )
            }

            ReportData(
                month = month,
                year = year,
                totalIncome = income,
                totalExpense = expense,
                netBalance = net,
                categoryExpenses = expenseShares,
                monthlyTrends = trends
            )
        }
    }
}
