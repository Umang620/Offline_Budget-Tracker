package com.example.magtipidka.domain.usecase.recurring

import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.domain.repository.RecurringBillRepository
import com.example.magtipidka.domain.repository.TransactionRepository
import java.util.Calendar

class ProcessDueRecurringBillsUseCase(
    private val recurringBillRepository: RecurringBillRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend fun getDueBillsForApproval(): List<RecurringBill> {
        val currentTime = System.currentTimeMillis()
        val allBills = recurringBillRepository.getDueRecurringBills(currentTime)

        return allBills.filter { bill ->
            bill.lastProcessedDate < bill.nextDueDate
        }
    }

    suspend fun approveAndPostBill(bill: RecurringBill): Long {
        val transaction = Transaction(
            type = TransactionType.EXPENSE,
            amount = bill.amount,
            categoryId = bill.categoryId,
            categoryName = bill.categoryName ?: "Bills & Wi-Fi",
            description = "Auto-Bill: ${bill.title}",
            date = System.currentTimeMillis()
        )
        val transactionId = transactionRepository.insertTransaction(transaction)

        val cal = Calendar.getInstance().apply { timeInMillis = bill.nextDueDate }
        if (bill.frequency.name == "WEEKLY") {
            cal.add(Calendar.DAY_OF_YEAR, 7)
        } else {
            cal.add(Calendar.MONTH, 1)
        }

        val updatedBill = bill.copy(
            nextDueDate = cal.timeInMillis,
            lastProcessedDate = System.currentTimeMillis()
        )
        recurringBillRepository.updateRecurringBill(updatedBill)

        return transactionId
    }
}
