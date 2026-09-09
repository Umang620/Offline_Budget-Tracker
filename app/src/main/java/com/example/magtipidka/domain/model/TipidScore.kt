package com.example.magtipidka.domain.model

enum class TipidScoreLevel {
    EXCELLENT,
    GOOD,
    FAIR,
    NEEDS_IMPROVEMENT
}

data class TipidScore(
    val score: Int, // 0 to 100
    val level: TipidScoreLevel,
    val insightMessage: String
)
