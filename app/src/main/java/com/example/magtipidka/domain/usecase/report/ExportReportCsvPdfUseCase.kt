package com.example.magtipidka.domain.usecase.report

import com.example.magtipidka.domain.model.ReportData
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.presentation.components.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportReportCsvPdfUseCase {

    fun generateCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,Amount,Description\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        transactions.forEach { t ->
            val dateStr = dateFormat.format(Date(t.date))
            val cleanDesc = t.description.replace(",", " ")
            val cleanCat = t.categoryName.replace(",", " ")
            sb.append("${t.id},$dateStr,${t.type.name},$cleanCat,${t.amount},$cleanDesc\n")
        }

        return sb.toString()
    }

    fun generateFormattedSummaryText(report: ReportData, currencySymbol: String = "₱"): String {
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("     MAG TIPID KA - FINANCIAL STATEMENT  \n")
        sb.append("=========================================\n\n")
        sb.append("Month/Year: ${report.month}/${report.year}\n")
        sb.append("Total Income:  ${formatCurrency(report.totalIncome, currencySymbol)}\n")
        sb.append("Total Expense: ${formatCurrency(report.totalExpense, currencySymbol)}\n")
        sb.append("Net Balance:   ${formatCurrency(report.netBalance, currencySymbol)}\n\n")

        sb.append("--- Category Spending Breakdown ---\n")
        report.categoryExpenses.forEach { cat ->
            sb.append("- ${cat.categoryName}: ${formatCurrency(cat.amount, currencySymbol)} (%.1f%%)\n".format(cat.percentage))
        }

        sb.append("\nGenerated offline by Mag Tipid Ka App.\n")
        return sb.toString()
    }
}
