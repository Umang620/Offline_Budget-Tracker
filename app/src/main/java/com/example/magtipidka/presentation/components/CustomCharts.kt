package com.example.magtipidka.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.magtipidka.domain.model.CategoryExpenseShare
import com.example.magtipidka.presentation.theme.EmeraldPrimary
import com.example.magtipidka.presentation.theme.ExpenseRed
import com.example.magtipidka.presentation.theme.GoldAccent
import com.example.magtipidka.presentation.theme.IncomeGreen
import com.example.magtipidka.presentation.theme.SecondaryTeal

private val ChartColors = listOf(
    EmeraldPrimary,
    GoldAccent,
    SecondaryTeal,
    ExpenseRed,
    IncomeGreen,
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFEC4899),
    Color(0xFFF59E0B),
    Color(0xFF3B82F6)
)

@Composable
fun CategoryPieChart(
    expenses: List<CategoryExpenseShare>,
    currencySymbol: String = "₱",
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expense data recorded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = expenses.sumOf { it.amount }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                var startAngle = -90f
                expenses.forEachIndexed { index, share ->
                    val sweepAngle = (share.amount / total * 360f).toFloat()
                    val color = ChartColors[index % ChartColors.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }

            // Centered Total Spending Focal Point
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCurrency(total, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breakdown List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            expenses.forEachIndexed { index, share ->
                val color = ChartColors[index % ChartColors.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = share.categoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${formatCurrency(share.amount, currencySymbol)} (%.1f%%)".format(share.percentage),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeVsExpenseBarChart(
    income: Double,
    expense: Double,
    currencySymbol: String = "₱",
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(income, expense, 1.0)
    val incomeRatio = (income / maxVal).toFloat()
    val expenseRatio = (expense / maxVal).toFloat()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Income vs. Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val barWidth = 44.dp.toPx()
                val canvasWidth = size.width
                val canvasHeight = size.height

                val incomeBarHeight = canvasHeight * incomeRatio
                val expenseBarHeight = canvasHeight * expenseRatio

                val incomeX = (canvasWidth / 3) - (barWidth / 2)
                val expenseX = (2 * canvasWidth / 3) - (barWidth / 2)

                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(incomeX, canvasHeight - incomeBarHeight),
                    size = Size(barWidth, incomeBarHeight),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )

                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(expenseX, canvasHeight - expenseBarHeight),
                    size = Size(barWidth, expenseBarHeight),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Income", style = MaterialTheme.typography.labelMedium, color = IncomeGreen, fontWeight = FontWeight.Bold)
                    Text(text = formatCurrency(income, currencySymbol), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Expense", style = MaterialTheme.typography.labelMedium, color = ExpenseRed, fontWeight = FontWeight.Bold)
                    Text(text = formatCurrency(expense, currencySymbol), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
