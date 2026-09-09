package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.repository.TransactionRepository

class DeleteTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }

    suspend operator fun invoke(id: Long) {
        repository.deleteTransactionById(id)
    }
}
