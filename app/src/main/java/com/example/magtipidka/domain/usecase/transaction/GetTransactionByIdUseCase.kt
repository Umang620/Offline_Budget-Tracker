package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.repository.TransactionRepository

class GetTransactionByIdUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Transaction? {
        return repository.getTransactionById(id)
    }
}
