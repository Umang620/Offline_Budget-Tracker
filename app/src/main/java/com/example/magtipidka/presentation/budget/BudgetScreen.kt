package com.example.magtipidka.presentation.budget

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.magtipidka.domain.usecase.budget.BudgetProgress
import com.example.magtipidka.presentation.components.EmptyState
import com.example.magtipidka.presentation.components.ProgressBarCard
import com.example.magtipidka.presentation.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isAddEditBudgetDialogOpen) {
        AddEditBudgetDialog(
            uiState = uiState,
            onAmountChanged = { viewModel.onInputAmountChanged(it) },
            onCategorySelected = { viewModel.onCategorySelected(it) },
            onPeriodSelected = { viewModel.onBudgetPeriodSelected(it) },
            onSave = { viewModel.onSaveBudget() },
            onDismiss = { viewModel.onDismissBudgetDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Budget Planner",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.monthName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                onClick = { viewModel.onOpenAddCategoryBudgetDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category Budget")
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

                // Budget Period Selector (Monthly / Weekly / Daily) Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedPeriod == BudgetPeriod.MONTHLY,
                            onClick = { viewModel.onBudgetPeriodSelected(BudgetPeriod.MONTHLY) },
                            label = { Text("Monthly", fontWeight = FontWeight.Bold) },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = uiState.selectedPeriod == BudgetPeriod.WEEKLY,
                            onClick = { viewModel.onBudgetPeriodSelected(BudgetPeriod.WEEKLY) },
                            label = { Text("Weekly", fontWeight = FontWeight.Bold) },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = uiState.selectedPeriod == BudgetPeriod.DAILY,
                            onClick = { viewModel.onBudgetPeriodSelected(BudgetPeriod.DAILY) },
                            label = { Text("Daily", fontWeight = FontWeight.Bold) },
                            shape = CircleShape
                        )
                    }
                }

                // Overall Budget Hero
                item {
                    val periodTitle = when (uiState.selectedPeriod) {
                        BudgetPeriod.MONTHLY -> "Monthly Budget Overview"
                        BudgetPeriod.WEEKLY -> "Weekly Allowance Overview"
                        BudgetPeriod.DAILY -> "Daily Spend Limit Overview"
                    }
                    Text(
                        text = periodTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    val overall = uiState.overallBudgetProgress
                    val cardTitle = when (uiState.selectedPeriod) {
                        BudgetPeriod.MONTHLY -> "Overall Monthly Budget"
                        BudgetPeriod.WEEKLY -> "Overall Weekly Budget"
                        BudgetPeriod.DAILY -> "Overall Daily Spend Limit"
                    }

                    if (overall != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                ProgressBarCard(
                                    title = cardTitle,
                                    currentAmount = overall.spentAmount,
                                    targetAmount = overall.budget.amount,
                                    currencySymbol = uiState.currencySymbol,
                                    progressPercentage = overall.progressPercentage,
                                    isExceeded = overall.isExceeded
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { viewModel.onOpenAddOverallBudgetDialog() }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Overall Budget",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { viewModel.onDeleteBudget(overall.budget) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Overall Budget",
                                            tint = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onOpenAddOverallBudgetDialog() },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Set Overall Allowance Budget",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Plan your total spending target for this period",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Category Budgets Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category Budgets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { viewModel.onOpenAddCategoryBudgetDialog() },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Limit")
                        }
                    }
                }

                if (uiState.categoryBudgetProgresses.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.AccountBalance,
                            title = "No category budgets set",
                            description = "Set specific limits for categories like Food, Transportation, or Shopping to prevent overspending.",
                            actionLabel = "Set Category Budget",
                            onActionClick = { viewModel.onOpenAddCategoryBudgetDialog() }
                        )
                    }
                } else {
                    items(uiState.categoryBudgetProgresses, key = { it.budget.id }) { progress ->
                        val categoryName = progress.budget.categoryName ?: "Category"
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                ProgressBarCard(
                                    title = categoryName,
                                    currentAmount = progress.spentAmount,
                                    targetAmount = progress.budget.amount,
                                    currencySymbol = uiState.currencySymbol,
                                    progressPercentage = progress.progressPercentage,
                                    isExceeded = progress.isExceeded
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { viewModel.onOpenAddCategoryBudgetDialog(progress) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Category Budget",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { viewModel.onDeleteBudget(progress.budget) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Category Budget",
                                            tint = ExpenseRed
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
private fun AddEditBudgetDialog(
    uiState: BudgetUiState,
    onAmountChanged: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onPeriodSelected: (BudgetPeriod) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = uiState.editingBudgetProgress != null
    val isCategoryMode = uiState.isCategoryBudgetMode
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedCategoryName = uiState.expenseCategories.find { it.id == uiState.selectedCategoryId }?.name ?: "Select Category"

    val periodLabel = when (uiState.selectedPeriod) {
        BudgetPeriod.DAILY -> "Daily"
        BudgetPeriod.WEEKLY -> "Weekly"
        BudgetPeriod.MONTHLY -> "Monthly"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    isEditing && isCategoryMode -> "Edit Category Budget ($periodLabel)"
                    isEditing -> "Edit Overall Budget ($periodLabel)"
                    isCategoryMode -> "Set Category Budget ($periodLabel)"
                    else -> "Set Overall Budget ($periodLabel)"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Period Cycle Goal Choice FilterChips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Budget Cycle Goal:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedPeriod == BudgetPeriod.DAILY,
                            onClick = { onPeriodSelected(BudgetPeriod.DAILY) },
                            label = { Text("Daily", style = MaterialTheme.typography.labelMedium) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.selectedPeriod == BudgetPeriod.WEEKLY,
                            onClick = { onPeriodSelected(BudgetPeriod.WEEKLY) },
                            label = { Text("Weekly", style = MaterialTheme.typography.labelMedium) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.selectedPeriod == BudgetPeriod.MONTHLY,
                            onClick = { onPeriodSelected(BudgetPeriod.MONTHLY) },
                            label = { Text("Monthly", style = MaterialTheme.typography.labelMedium) },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isCategoryMode) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(selectedCategoryName, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            uiState.expenseCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        onCategorySelected(category.id)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.inputAmount,
                    onValueChange = onAmountChanged,
                    label = { Text("Target $periodLabel Amount (${uiState.currencySymbol})") },
                    prefix = { Text("${uiState.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, shape = CircleShape) {
                Text("Save $periodLabel Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
