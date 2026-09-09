package com.example.magtipidka.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "INCOME", "EXPENSE", "ALL"
    val isDefault: Boolean = false,
    val iconName: String = "category",
    val colorHex: String = "#00523B"
)
