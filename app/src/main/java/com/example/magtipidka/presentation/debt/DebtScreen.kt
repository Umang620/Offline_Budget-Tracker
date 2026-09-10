package com.example.magtipidka.presentation.debt

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.magtipidka.domain.model.Debt
import com.example.magtipidka.domain.model.DebtType
import com.example.magtipidka.presentation.components.EmptyState
import com.example.magtipidka.presentation.components.formatCurrency
import com.example.magtipidka.presentation.theme.ExpenseRed
import com.example.magtipidka.presentation.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    viewModel: DebtViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isAddEditDebtDialogOpen) {
        AddEditDebtDialog(
            uiState = uiState,
            onNameChanged = { viewModel.onPersonNameChanged(it) },
            onAmountChanged = { viewModel.onAmountChanged(it) },
            onTypeChanged = { viewModel.onTypeChanged(it) },
            onDueDateChanged = { viewModel.onDueDateChanged(it) },
            onNoteChanged = { viewModel.onNoteChanged(it) },
            onSave = { viewModel.onSaveDebt() },
            onDismiss = { viewModel.onDismissDebtDialog() }
        )
    }

    if (uiState.isRecordPaymentDialogOpen) {
        RecordPaymentDialog(
            uiState = uiState,
            onAmountChanged = { viewModel.onPaymentAmountChanged(it) },
            onSave = { viewModel.onSavePayment() },
            onDismiss = { viewModel.onDismissPaymentDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Utang & Pautang (IOU)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                onClick = { viewModel.onOpenAddDebtDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Record")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.selectedFilterType == null,
                        onClick = { viewModel.onFilterTypeSelected(null) },
                        label = { Text("All Records") },
                        shape = CircleShape
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilterType == DebtType.IOWE,
                        onClick = { viewModel.onFilterTypeSelected(DebtType.IOWE) },
                        label = { Text("Utang (I Owe)") },
                        shape = CircleShape
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilterType == DebtType.OWED_TO_ME,
                        onClick = { viewModel.onFilterTypeSelected(DebtType.OWED_TO_ME) },
                        label = { Text("Pautang (Owed to Me)") },
                        shape = CircleShape
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.debts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Handshake,
                    title = "No IOU records found",
                    description = "Keep track of casual borrowing or lending with classmates, friends, or family.",
                    actionLabel = "Add Record",
                    onActionClick = { viewModel.onOpenAddDebtDialog() }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.debts, key = { it.id }) { debt ->
                        DebtItemCard(
                            debt = debt,
                            currencySymbol = uiState.currencySymbol,
                            onRecordPayment = { viewModel.onOpenRecordPaymentDialog(debt) },
                            onEdit = { viewModel.onOpenAddDebtDialog(debt) },
                            onDelete = { viewModel.onDeleteDebt(debt) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun DebtItemCard(
    debt: Debt,
    currencySymbol: String,
    onRecordPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUtang = debt.type == DebtType.IOWE
    val typeLabel = if (isUtang) "Utang (I Owe)" else "Pautang (Owed to Me)"
    val badgeColor = if (isUtang) ExpenseRed else IncomeGreen
    val progress = if (debt.amount > 0) (debt.paidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 0f
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDateStr = dateFormat.format(Date(debt.dueDate))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatCurrency(debt.amount, currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = badgeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid: ${formatCurrency(debt.paidAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (debt.isSettled) "Settled!" else "Due: $dueDateStr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (debt.isSettled) IncomeGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            if (debt.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${debt.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!debt.isSettled) {
                    Button(onClick = onRecordPayment, shape = CircleShape) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payment")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditDebtDialog(
    uiState: DebtUiState,
    onNameChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTypeChanged: (DebtType) -> Unit,
    onDueDateChanged: (Long) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(uiState.dueDateInput))
    val calendar = Calendar.getInstance().apply { timeInMillis = uiState.dueDateInput }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDueDateChanged(selectedCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (uiState.editingDebt != null) "Edit IOU Record" else "New IOU Record",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.typeInput == DebtType.IOWE,
                        onClick = { onTypeChanged(DebtType.IOWE) },
                        label = { Text("Utang (I Owe)") },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.typeInput == DebtType.OWED_TO_ME,
                        onClick = { onTypeChanged(DebtType.OWED_TO_ME) },
                        label = { Text("Pautang (Owed to Me)") },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = uiState.personNameInput,
                    onValueChange = onNameChanged,
                    label = { Text("Person's Name") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.amountInput,
                    onValueChange = onAmountChanged,
                    label = { Text("Amount (${uiState.currencySymbol})") },
                    prefix = { Text("${uiState.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Due Date: $formattedDate")
                }

                OutlinedTextField(
                    value = uiState.noteInput,
                    onValueChange = onNoteChanged,
                    label = { Text("Note / Description (Optional)") },
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
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    uiState: DebtUiState,
    onAmountChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val debt = uiState.payingDebt
    val remaining = if (debt != null) (debt.amount - debt.paidAmount).coerceAtLeast(0.0) else 0.0
    val isUtang = debt?.type == DebtType.IOWE
    val typeLabel = if (isUtang) "Utang Balance" else "Pautang Receivable"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Payment for '${debt?.personName}'", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (debt != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatCurrency(debt.amount, uiState.currencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Unsettled Remaining: ${formatCurrency(remaining, uiState.currencySymbol)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUtang) ExpenseRed else IncomeGreen
                    )
                }

                OutlinedTextField(
                    value = uiState.paymentAmountInput,
                    onValueChange = onAmountChanged,
                    label = { Text("Payment Amount (${uiState.currencySymbol})") },
                    prefix = { Text("${uiState.currencySymbol} ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
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
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
