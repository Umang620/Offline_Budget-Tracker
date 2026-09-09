package com.example.magtipidka.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import java.text.DecimalFormat
import kotlin.math.abs

@Composable
fun FormattedCurrencyText(
    amount: Double,
    currencySymbol: String = "₱",
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    val formattedText = formatCurrency(amount, currencySymbol)

    Text(
        text = formattedText,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = false
    )
}

fun formatCurrency(amount: Double, currencySymbol: String = "₱"): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
}

fun formatCompactCurrency(amount: Double, currencySymbol: String = "₱"): String {
    val absAmount = abs(amount)
    val sign = if (amount < 0) "-" else ""
    val formatter = DecimalFormat("#,##0.0")

    return when {
        absAmount >= 1_000_000_000 -> "$sign$currencySymbol${formatter.format(absAmount / 1_000_000_000.0)}B"
        absAmount >= 1_000_000 -> "$sign$currencySymbol${formatter.format(absAmount / 1_000_000.0)}M"
        absAmount >= 100_000 -> "$sign$currencySymbol${formatter.format(absAmount / 1_000.0)}K"
        else -> formatCurrency(amount, currencySymbol)
    }
}
