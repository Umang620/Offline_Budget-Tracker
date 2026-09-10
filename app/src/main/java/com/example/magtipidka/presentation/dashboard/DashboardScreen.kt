package com.example.magtipidka.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.magtipidka.domain.model.DailyLimitStatus
import com.example.magtipidka.domain.model.TipidScoreLevel
import com.example.magtipidka.domain.model.Transaction
import com.example.magtipidka.domain.model.TransactionType
import com.example.magtipidka.presentation.components.CategoryIcon
import com.example.magtipidka.presentation.components.CategoryPieChart
import com.example.magtipidka.presentation.components.EmptyState
import com.example.magtipidka.presentation.components.ProgressBarCard
import com.example.magtipidka.presentation.components.formatCurrency
import com.example.magtipidka.presentation.theme.ExpenseRed
import com.example.magtipidka.presentation.theme.GoldAccent
import com.example.magtipidka.presentation.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToRecurringBills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val greeting = rememberGreeting()

    // Recurring Bill Auto-Posting Approval Dialog
    val dueBill = uiState.dueRecurringBillsForApproval.firstOrNull()
    if (dueBill != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onSkipBill(dueBill) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recurring Bill Due Today", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your scheduled bill '${dueBill.title}' (${formatCurrency(dueBill.amount, uiState.currencySymbol)}) is due today.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Would you like to log this as an expense transaction now?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onApproveAndPostBill(dueBill) },
                    shape = CircleShape
                ) {
                    Text("Approve & Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onSkipBill(dueBill) }) {
                    Text("Skip For Now")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mag Tipid Ka",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // High Contrast Hero Balance Container
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                        tonalElevation = 2.dp,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Current Balance",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrency(uiState.currentBalance, uiState.currencySymbol),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (uiState.totalUtang > 0 || uiState.totalPautang > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.50f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Net: ${formatCurrency(uiState.netBalance, uiState.currencySymbol)} ",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        if (uiState.totalUtang > 0) {
                                            Text(
                                                text = "(-${formatCurrency(uiState.totalUtang, uiState.currencySymbol)} Utang)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseRed
                                            )
                                        }
                                        if (uiState.totalUtang > 0 && uiState.totalPautang > 0) {
                                            Text(
                                                text = " ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        if (uiState.totalPautang > 0) {
                                            Text(
                                                text = "(+${formatCurrency(uiState.totalPautang, uiState.currencySymbol)} Pautang)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "This Month's Cashflow",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Dual Monthly Cashflow High-Contrast Outlined Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.5.dp, IncomeGreen.copy(alpha = 0.50f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = IncomeGreen)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            Text(
                                                formatCurrency(uiState.totalIncome, uiState.currencySymbol),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.5.dp, ExpenseRed.copy(alpha = 0.50f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = ExpenseRed)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            Text(
                                                formatCurrency(uiState.totalExpenses, uiState.currencySymbol),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseRed,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Segmented 3-Tab Filter Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedTab == DashboardTab.OVERVIEW,
                            onClick = { viewModel.onTabSelected(DashboardTab.OVERVIEW) },
                            label = { Text("Overview", fontWeight = FontWeight.Bold) },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = uiState.selectedTab == DashboardTab.BUDGETS_GOALS,
                            onClick = { viewModel.onTabSelected(DashboardTab.BUDGETS_GOALS) },
                            label = { Text("Goals & Budgets", fontWeight = FontWeight.Bold) },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = uiState.selectedTab == DashboardTab.COMMITMENTS,
                            onClick = { viewModel.onTabSelected(DashboardTab.COMMITMENTS) },
                            label = { Text("IOUs & Bills", fontWeight = FontWeight.Bold) },
                            shape = CircleShape
                        )
                    }
                }

                // TAB 1: OVERVIEW CONTENT
                if (uiState.selectedTab == DashboardTab.OVERVIEW) {
                    // Tipid Score Financial Health Index
                    item {
                        val scoreObj = uiState.tipidScore
                        if (scoreObj != null) {
                            val scoreBadgeColor = when (scoreObj.level) {
                                TipidScoreLevel.EXCELLENT -> IncomeGreen
                                TipidScoreLevel.GOOD -> MaterialTheme.colorScheme.primary
                                TipidScoreLevel.FAIR -> GoldAccent
                                TipidScoreLevel.NEEDS_IMPROVEMENT -> ExpenseRed
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, scoreBadgeColor.copy(alpha = 0.65f)),
                                tonalElevation = 2.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = scoreBadgeColor,
                                        modifier = Modifier.height(28.dp).width(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Tipid Financial Score",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${scoreObj.score} / 100",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = scoreBadgeColor,
                                                maxLines = 1
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = scoreObj.insightMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Daily Safe-To-Spend Limit Outlined Container
                    item {
                        val dailyLimit = uiState.dailySpendLimit
                        if (dailyLimit != null) {
                            val statusColor = when (dailyLimit.status) {
                                DailyLimitStatus.SAFE -> IncomeGreen
                                DailyLimitStatus.WARNING -> GoldAccent
                                DailyLimitStatus.EXCEEDED -> ExpenseRed
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.65f)),
                                tonalElevation = 2.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.height(28.dp).width(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Daily Safe Spend Limit",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${formatCurrency(dailyLimit.safeDailyLimit, uiState.currencySymbol)} / day",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${formatCurrency(dailyLimit.remainingBudget, uiState.currencySymbol)} left across ${dailyLimit.daysRemainingInMonth} remaining days",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category Spending Donut Chart
                    item {
                        if (uiState.topCategoryExpenses.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                tonalElevation = 2.dp,
                                shadowElevation = 0.dp
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Spending Breakdown",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    CategoryPieChart(
                                        expenses = uiState.topCategoryExpenses,
                                        currencySymbol = uiState.currencySymbol
                                    )
                                }
                            }
                        }
                    }

                    // #1 Most Spent Category Insight Banner
                    item {
                        val topShare = uiState.topExpenseCategoryShare
                        if (topShare != null && topShare.amount > 0) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                tonalElevation = 2.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.height(26.dp).width(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "#1 Expense This Month",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${topShare.categoryName} (${formatCurrency(topShare.amount, uiState.currencySymbol)})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Represents %.1f%% of your total monthly expenses".format(topShare.percentage),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recent Transactions Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.clickable { onNavigateToTransactions() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Recent Transactions List (5 Recent)
                    if (uiState.recentTransactions.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.Receipt,
                                title = "No transactions yet",
                                description = "Tap the '+' button to log your first income or expense!",
                                actionLabel = "Add Transaction",
                                onActionClick = onNavigateToAddTransaction
                            )
                        }
                    } else {
                        items(uiState.recentTransactions, key = { it.id }) { transaction ->
                            DashboardTransactionItem(
                                transaction = transaction,
                                currencySymbol = uiState.currencySymbol
                            )
                        }
                    }
                }

                // TAB 2: GOALS & BUDGETS CONTENT
                if (uiState.selectedTab == DashboardTab.BUDGETS_GOALS) {
                    // Monthly Budget Progress Section
                    item {
                        val budgetProgress = uiState.overallBudgetProgress
                        if (budgetProgress != null) {
                            ProgressBarCard(
                                title = "Monthly Budget",
                                currentAmount = budgetProgress.spentAmount,
                                targetAmount = budgetProgress.budget.amount,
                                currencySymbol = uiState.currencySymbol,
                                progressPercentage = budgetProgress.progressPercentage,
                                isExceeded = budgetProgress.isExceeded,
                                subtitle = "${formatCurrency(budgetProgress.remainingAmount, uiState.currencySymbol)} remaining",
                                modifier = Modifier.clickable { onNavigateToBudget() }
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToBudget() },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                tonalElevation = 2.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Set Monthly Budget",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Plan your expenses allowance for this month",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // Savings Goals Overview Card
                    item {
                        val goals = uiState.savingsGoals
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSavings() },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                            tonalElevation = 2.dp,
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.height(22.dp).width(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Savings Goals Overview",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Manage",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (goals.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        goals.take(3).forEach { goal ->
                                            val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                                            val isCompleted = goal.currentAmount >= goal.targetAmount

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = goal.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = "${formatCurrency(goal.currentAmount, uiState.currencySymbol)} / ${formatCurrency(goal.targetAmount, uiState.currencySymbol)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(CircleShape),
                                                    color = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No active savings goals set. Tap to start saving for your target milestones!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 3: COMMITMENTS CONTENT
                if (uiState.selectedTab == DashboardTab.COMMITMENTS) {
                    // Commitments & IOUs Summary Outlined Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Commitments & IOUs Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToDebts() },
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, ExpenseRed.copy(alpha = 0.50f)),
                                    tonalElevation = 2.dp,
                                    shadowElevation = 0.dp
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Handshake,
                                                contentDescription = null,
                                                tint = ExpenseRed,
                                                modifier = Modifier.height(16.dp).width(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Utang", style = MaterialTheme.typography.labelMedium, color = ExpenseRed, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(uiState.totalUtang, uiState.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToDebts() },
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, IncomeGreen.copy(alpha = 0.50f)),
                                    tonalElevation = 2.dp,
                                    shadowElevation = 0.dp
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Handshake,
                                                contentDescription = null,
                                                tint = IncomeGreen,
                                                modifier = Modifier.height(16.dp).width(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pautang", style = MaterialTheme.typography.labelMedium, color = IncomeGreen, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(uiState.totalPautang, uiState.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToRecurringBills() },
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)),
                                    tonalElevation = 2.dp,
                                    shadowElevation = 0.dp
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EventRepeat,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.height(16.dp).width(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Bills", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(uiState.totalBillsToPay, uiState.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // All-Time Lifetime Totals Container
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                            tonalElevation = 2.dp,
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.height(20.dp).width(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "All-Time Lifetime Totals",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Total Income Received",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(uiState.allTimeIncome, uiState.currencySymbol),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Total Money Spent",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(uiState.allTimeExpenses, uiState.currencySymbol),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun DashboardTransactionItem(
    transaction: Transaction,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val amountPrefix = if (isIncome) "+" else "-"

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(transaction.date))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                iconName = if (isIncome) "account_balance_wallet" else "receipt_long",
                contentDescription = null,
                containerColor = amountColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$amountPrefix${formatCurrency(transaction.amount, currencySymbol)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning ☀️"
        in 12..17 -> "Good Afternoon 🌤️"
        else -> "Good Evening 🌙"
    }
}
