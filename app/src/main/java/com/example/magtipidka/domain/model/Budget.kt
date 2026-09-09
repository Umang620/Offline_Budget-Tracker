package com.example.magtipidka.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val amount: Double,
    val month: Int,
    val year: Int
)
