package com.example.magtipidka.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val iconName: String = "category"
)
