package com.example.magtipidka.domain.engine

import com.example.magtipidka.domain.model.Category

object CategoryKeywordEngine {

    private val rules = mapOf(
        "Food & Canteen" to listOf(
            "jollibee", "mcdo", "mcdonald", "kfc", "canteen", "chowking", "mang inasal", "7-eleven",
            "711", "tapsilog", "siomai", "coffee", "milktea", "food", "lunch", "dinner", "breakfast",
            "snack", "burger", "pizza", "lugaw", "pares", "bakery", "bread", "coke"
        ),
        "Jeep & Transportation" to listOf(
            "jeep", "jeepney", "bus", "mrt", "lrt", "grab", "angkas", "joyride", "tricycle",
            "fare", "pamasahe", "gas", "petrol", "shell", "petron", "caltex", "toll", "parking", "taxi"
        ),
        "School & Tuition" to listOf(
            "tuition", "school", "exam", "prelim", "midterm", "finals", "univ", "college",
            "reg", "enrollment", "laboratory", "lab fee"
        ),
        "Books & Supplies" to listOf(
            "book", "notebook", "pen", "paper", "pencil", "supplies", "national bookstore",
            "module", "photocopy"
        ),
        "Projects & Printing" to listOf(
            "print", "printing", "project", "tarpaulin", "bind", "binding", "craft", "cartolina"
        ),
        "Rent & Dorm" to listOf(
            "rent", "dorm", "boarding", "apartment", "landlord", "bedspace"
        ),
        "Bills & Wi-Fi" to listOf(
            "meralco", "pldt", "globe", "smart", "converge", "maynilad", "water", "electricity",
            "wifi", "internet", "bill", "load", "load promo"
        ),
        "Shopping" to listOf(
            "shopee", "lazada", "tiktok shop", "uniqlo", "mall", "clothes", "shirt", "shoes",
            "pants", "dress", "sartitorial"
        ),
        "Hangout & Fun" to listOf(
            "movie", "cinema", "game", "steam", "valorant", "ml", "mobile legends", "netflix",
            "spotify", "concert", "arcade"
        ),
        "Health & Medical" to listOf(
            "doctor", "hospital", "medicine", "pharmacy", "mercury drug", "watsons", "vitamin", "clinic"
        ),
        "Allowance" to listOf(
            "allowance", "padala", "baon", "mama", "papa", "parents", "nanay", "tatay"
        ),
        "Scholarship" to listOf(
            "scholarship", "dost", "ched", "stipend", "grant"
        ),
        "Parents / Family" to listOf(
            "parents", "family", "relative", "kuya", "ate", "tita", "tito"
        ),
        "Side Hustle / Part-time" to listOf(
            "side hustle", "freelance", "part-time", "part time", "commission", "commissioned"
        ),
        "Gifts & Pamasko" to listOf(
            "gift", "pamasko", "birthday", "aginaldo", "bonus"
        )
    )

    fun findMatchingCategory(description: String, availableCategories: List<Category>): Category? {
        if (description.isBlank() || availableCategories.isEmpty()) return null
        val lowerText = description.lowercase().trim()

        // 1. Direct Rule Matching
        for ((categoryName, keywords) in rules) {
            if (keywords.any { lowerText.contains(it) }) {
                val matchedCat = availableCategories.find { it.name.equals(categoryName, ignoreCase = true) }
                if (matchedCat != null) return matchedCat
            }
        }

        // 2. Exact or Partial Match against available category names
        return availableCategories.find { lowerText.contains(it.name.lowercase()) }
    }
}
