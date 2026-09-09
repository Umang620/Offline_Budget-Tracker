package com.example.magtipidka.data.mapper

import com.example.magtipidka.data.local.dao.BudgetWithCategory
import com.example.magtipidka.data.local.dao.RecurringBillWithCategory
import com.example.magtipidka.data.local.dao.TransactionWithCategory
import com.example.magtipidka.data.local.entity.BudgetEntity
import com.example.magtipidka.data.local.entity.CategoryEntity
import com.example.magtipidka.data.local.entity.DebtEntity
import com.example.magtipidka.data.local.entity.RecurringBillEntity
import com.example.magtipidka.data.local.entity.SavingsGoalEntity
import com.example.magtipidka.data.local.entity.TransactionEntity
import com.example.magtipidka.domain.model.Budget
import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.model.Debt
import com.example.magtipidka.domain.model.DebtType
import com.example.magtipidka.domain.model.RecurringBill
import com.example.magtipidka.domain.model.RecurringFrequency
import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = try { CategoryType.valueOf(type) } catch (e: Exception) { CategoryType.EXPENSE },
        isDefault = isDefault,
        iconName = iconName
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.name,
        isDefault = isDefault,
        iconName = iconName,
        colorHex = "#00523B"
    )
}

fun TransactionWithCategory.toDomain(): Transaction {
    return Transaction(
        id = transaction.id,
        type = try { TransactionType.valueOf(transaction.type) } catch (e: Exception) { TransactionType.EXPENSE },
        amount = transaction.amount,
        categoryId = transaction.categoryId,
        categoryName = categoryName ?: "Uncategorized",
        description = transaction.description,
        date = transaction.date,
        receiptImageUri = transaction.receiptImageUri,
        createdAt = transaction.createdAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type.name,
        amount = amount,
        categoryId = categoryId,
        description = description,
        date = date,
        receiptImageUri = receiptImageUri,
        createdAt = createdAt
    )
}

fun BudgetWithCategory.toDomain(): Budget {
    return Budget(
        id = id,
        categoryId = categoryId,
        categoryName = categoryName,
        amount = amount,
        month = month,
        year = year
    )
}

fun BudgetEntity.toDomain(categoryName: String? = null): Budget {
    return Budget(
        id = id,
        categoryId = categoryId,
        categoryName = categoryName,
        amount = amount,
        month = month,
        year = year
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        categoryId = categoryId,
        amount = amount,
        month = month,
        year = year
    )
}

fun SavingsGoalEntity.toDomain(): SavingsGoal {
    return SavingsGoal(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        targetDate = targetDate
    )
}

fun SavingsGoal.toEntity(): SavingsGoalEntity {
    return SavingsGoalEntity(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        targetDate = targetDate
    )
}

fun DebtEntity.toDomain(): Debt {
    return Debt(
        id = id,
        personName = personName,
        amount = amount,
        paidAmount = paidAmount,
        type = try { DebtType.valueOf(type) } catch (e: Exception) { DebtType.IOWE },
        dueDate = dueDate,
        isSettled = isSettled,
        note = note,
        createdAt = createdAt
    )
}

fun Debt.toEntity(): DebtEntity {
    return DebtEntity(
        id = id,
        personName = personName,
        amount = amount,
        paidAmount = paidAmount,
        type = type.name,
        dueDate = dueDate,
        isSettled = isSettled,
        note = note,
        createdAt = createdAt
    )
}

fun RecurringBillWithCategory.toDomain(): RecurringBill {
    return RecurringBill(
        id = id,
        title = title,
        amount = amount,
        categoryId = categoryId,
        categoryName = categoryName ?: "Uncategorized",
        frequency = try { RecurringFrequency.valueOf(frequency) } catch (e: Exception) { RecurringFrequency.MONTHLY },
        nextDueDate = nextDueDate,
        isAutoAdd = isAutoAdd,
        lastProcessedDate = 0L,
        createdAt = createdAt
    )
}

fun RecurringBillEntity.toDomain(categoryName: String? = null): RecurringBill {
    return RecurringBill(
        id = id,
        title = title,
        amount = amount,
        categoryId = categoryId,
        categoryName = categoryName,
        frequency = try { RecurringFrequency.valueOf(frequency) } catch (e: Exception) { RecurringFrequency.MONTHLY },
        nextDueDate = nextDueDate,
        isAutoAdd = isAutoAdd,
        lastProcessedDate = lastProcessedDate,
        createdAt = createdAt
    )
}

fun RecurringBill.toEntity(): RecurringBillEntity {
    return RecurringBillEntity(
        id = id,
        title = title,
        amount = amount,
        categoryId = categoryId,
        frequency = frequency.name,
        nextDueDate = nextDueDate,
        isAutoAdd = isAutoAdd,
        lastProcessedDate = lastProcessedDate,
        createdAt = createdAt
    )
}
