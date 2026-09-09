package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.repository.TransactionRepository

class AddTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Long {
        require(transaction.amount > 0) { "Amount must be greater than zero" }
        require(transaction.categoryId > 0) { "Category must be selected" }
        return repository.insertTransaction(transaction)
    }
}
