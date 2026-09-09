package com.example.magtipidka.domain.model

data class BackupData(
    val version: Int = 2,
    val exportDate: Long = System.currentTimeMillis(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val recurringBills: List<RecurringBill> = emptyList()
)
