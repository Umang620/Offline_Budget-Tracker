package com.example.magtipidka.data.local.database

import com.example.magtipidka.data.local.entity.CategoryEntity

object DefaultCategories {
    val list = listOf(
        // Student & Everyday Expense Categories
        CategoryEntity(name = "Food & Canteen", type = "EXPENSE", isDefault = true, iconName = "fastfood"),
        CategoryEntity(name = "Jeep & Transportation", type = "EXPENSE", isDefault = true, iconName = "directions_bus"),
        CategoryEntity(name = "School & Tuition", type = "EXPENSE", isDefault = true, iconName = "school"),
        CategoryEntity(name = "Books & Supplies", type = "EXPENSE", isDefault = true, iconName = "menu_book"),
        CategoryEntity(name = "Projects & Printing", type = "EXPENSE", isDefault = true, iconName = "print"),
        CategoryEntity(name = "Rent & Dorm", type = "EXPENSE", isDefault = true, iconName = "home"),
        CategoryEntity(name = "Bills & Wi-Fi", type = "EXPENSE", isDefault = true, iconName = "receipt_long"),
        CategoryEntity(name = "Shopping", type = "EXPENSE", isDefault = true, iconName = "shopping_bag"),
        CategoryEntity(name = "Hangout & Fun", type = "EXPENSE", isDefault = true, iconName = "sports_esports"),
        CategoryEntity(name = "Health & Medical", type = "EXPENSE", isDefault = true, iconName = "medical_services"),
        CategoryEntity(name = "Other Expense", type = "EXPENSE", isDefault = true, iconName = "more_horiz"),

        // Student & Personal Income Categories
        CategoryEntity(name = "Allowance", type = "INCOME", isDefault = true, iconName = "account_balance_wallet"),
        CategoryEntity(name = "Scholarship", type = "INCOME", isDefault = true, iconName = "school"),
        CategoryEntity(name = "Parents / Family", type = "INCOME", isDefault = true, iconName = "family_restroom"),
        CategoryEntity(name = "Side Hustle / Part-time", type = "INCOME", isDefault = true, iconName = "work"),
        CategoryEntity(name = "Gifts & Pamasko", type = "INCOME", isDefault = true, iconName = "card_giftcard"),
        CategoryEntity(name = "Salary", type = "INCOME", isDefault = true, iconName = "payments"),
        CategoryEntity(name = "Business", type = "INCOME", isDefault = true, iconName = "storefront"),
        CategoryEntity(name = "Other Income", type = "INCOME", isDefault = true, iconName = "savings")
    )
}
