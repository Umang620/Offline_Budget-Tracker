package com.example.magtipidka.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    val imageVector = getCategoryVector(iconName)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}

fun getCategoryVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "fastfood", "food" -> Icons.Default.Fastfood
        "directions_bus", "transportation" -> Icons.Default.DirectionsBus
        "receipt_long", "bills" -> Icons.Default.ReceiptLong
        "school" -> Icons.Default.School
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "sports_esports", "entertainment" -> Icons.Default.SportsEsports
        "medical_services", "health" -> Icons.Default.MedicalServices
        "account_balance_wallet", "allowance" -> Icons.Default.AccountBalanceWallet
        "payments", "salary" -> Icons.Default.Payments
        "storefront", "business" -> Icons.Default.Storefront
        "trending_up", "investment" -> Icons.Default.TrendingUp
        "savings" -> Icons.Default.Savings
        "more_horiz" -> Icons.Default.MoreHoriz
        else -> Icons.Default.Category
    }
}
