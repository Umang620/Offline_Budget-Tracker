package com.example.magtipidka.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.Receipt)
    object Budget : Screen("budget", "Budget", Icons.Default.AccountBalance)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
    object SavingsGoals : Screen("savings_goals")
    object Debts : Screen("debts")
    object RecurringBills : Screen("recurring_bills")
    object License : Screen("license")
    object Pin : Screen("pin")
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Budget,
    Screen.Reports,
    Screen.Settings
)
