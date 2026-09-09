package com.example.magtipidka.domain.usecase.transaction

import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }

    fun byMonthYear(month: Int, year: Int): Flow<List<Transaction>> {
        return repository.getTransactionsByMonthYear(month, year)
    }

    fun search(query: String): Flow<List<Transaction>> {
        return repository.searchTransactions(query)
    }

    fun filter(type: TransactionType?, categoryId: Long?, startDate: Long?, endDate: Long?): Flow<List<Transaction>> {
        return repository.filterTransactions(type, categoryId, startDate, endDate)
    }
}
