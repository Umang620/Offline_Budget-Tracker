package com.example.magtipidka.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.Debt
import com.example.magtipidka.domain.model.DebtType
import com.example.magtipidka.domain.repository.DebtRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DebtViewModel(
    private val debtRepository: DebtRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedFilterType = MutableStateFlow<DebtType?>(null)
    private val _isAddEditDebtDialogOpen = MutableStateFlow(false)
    private val _editingDebt = MutableStateFlow<Debt?>(null)
    private val _personNameInput = MutableStateFlow("")
    private val _amountInput = MutableStateFlow("")
    private val _typeInput = MutableStateFlow(DebtType.IOWE)
    private val _dueDateInput = MutableStateFlow(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))
    private val _noteInput = MutableStateFlow("")

    private val _isRecordPaymentDialogOpen = MutableStateFlow(false)
    private val _payingDebt = MutableStateFlow<Debt?>(null)
    private val _paymentAmountInput = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    init {
        observeDebtsData()
    }

    private fun observeDebtsData() {
        val debtsFlow = debtRepository.getAllDebts().combine(_selectedFilterType) { list, filterType ->
            if (filterType == null) list else list.filter { it.type == filterType }
        }

        val fieldsFlow = combine(
            _personNameInput,
            _amountInput,
            _typeInput,
            _dueDateInput,
            _noteInput
        ) { name, amount, type, date, note ->
            DebtFields(name, amount, type, date, note)
        }

        val dialogStateFlow = combine(
            _isAddEditDebtDialogOpen,
            _editingDebt,
            fieldsFlow
        ) { open, editing, fields ->
            DebtDialogState(open, editing, fields.name, fields.amount, fields.type, fields.date, fields.note)
        }

        val paymentStateFlow = combine(
            _isRecordPaymentDialogOpen,
            _payingDebt,
            _paymentAmountInput
        ) { open, debt, amount ->
            PaymentDialogState(open, debt, amount)
        }

        val metaFlow = combine(
            debtsFlow,
            settingsRepository.getSettings(),
            _selectedFilterType
        ) { debts, settings, filterType ->
            Triple(debts, settings.currencySymbol, filterType)
        }

        combine(
            metaFlow,
            dialogStateFlow,
            paymentStateFlow,
            _errorMessage
        ) { meta, dialogState, paymentState, error ->
            DebtUiState(
                isLoading = false,
                currencySymbol = meta.second,
                selectedFilterType = meta.third,
                debts = meta.first,
                isAddEditDebtDialogOpen = dialogState.open,
                editingDebt = dialogState.editing,
                personNameInput = dialogState.name,
                amountInput = dialogState.amount,
                typeInput = dialogState.type,
                dueDateInput = dialogState.date,
                noteInput = dialogState.note,
                isRecordPaymentDialogOpen = paymentState.open,
                payingDebt = paymentState.debt,
                paymentAmountInput = paymentState.amount,
                errorMessage = error
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private data class DebtFields(
        val name: String,
        val amount: String,
        val type: DebtType,
        val date: Long,
        val note: String
    )

    private data class DebtDialogState(
        val open: Boolean,
        val editing: Debt?,
        val name: String,
        val amount: String,
        val type: DebtType,
        val date: Long,
        val note: String
    )

    private data class PaymentDialogState(
        val open: Boolean,
        val debt: Debt?,
        val amount: String
    )

    fun onFilterTypeSelected(type: DebtType?) {
        _selectedFilterType.value = type
    }

    fun onOpenAddDebtDialog(existingDebt: Debt? = null) {
        _editingDebt.value = existingDebt
        _personNameInput.value = existingDebt?.personName ?: ""
        _amountInput.value = existingDebt?.amount?.toString() ?: ""
        _typeInput.value = existingDebt?.type ?: DebtType.IOWE
        _dueDateInput.value = existingDebt?.dueDate ?: (System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))
        _noteInput.value = existingDebt?.note ?: ""
        _errorMessage.value = null
        _isAddEditDebtDialogOpen.value = true
    }

    fun onDismissDebtDialog() {
        _isAddEditDebtDialogOpen.value = false
    }

    fun onPersonNameChanged(name: String) { _personNameInput.value = name; _errorMessage.value = null }
    fun onAmountChanged(amount: String) { _amountInput.value = amount; _errorMessage.value = null }
    fun onTypeChanged(type: DebtType) { _typeInput.value = type }
    fun onDueDateChanged(date: Long) { _dueDateInput.value = date }
    fun onNoteChanged(note: String) { _noteInput.value = note }

    fun onSaveDebt() {
        val name = _personNameInput.value.trim()
        if (name.isEmpty()) { _errorMessage.value = "Please enter a person's name."; return }

        val amountVal = _amountInput.value.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) { _errorMessage.value = "Please enter a valid amount."; return }

        val debt = Debt(
            id = _editingDebt.value?.id ?: 0L,
            personName = name,
            amount = amountVal,
            paidAmount = _editingDebt.value?.paidAmount ?: 0.0,
            type = _typeInput.value,
            dueDate = _dueDateInput.value,
            note = _noteInput.value.trim()
        )

        viewModelScope.launch {
            try {
                if (_editingDebt.value != null) {
                    debtRepository.updateDebt(debt)
                } else {
                    debtRepository.insertDebt(debt)
                }
                _isAddEditDebtDialogOpen.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save record."
            }
        }
    }

    fun onOpenRecordPaymentDialog(debt: Debt) {
        _payingDebt.value = debt
        _paymentAmountInput.value = ""
        _errorMessage.value = null
        _isRecordPaymentDialogOpen.value = true
    }

    fun onDismissPaymentDialog() {
        _isRecordPaymentDialogOpen.value = false
    }

    fun onPaymentAmountChanged(amount: String) {
        _paymentAmountInput.value = amount
        _errorMessage.value = null
    }

    fun onSavePayment() {
        val amountVal = _paymentAmountInput.value.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            _errorMessage.value = "Please enter a valid payment amount."
            return
        }

        val debt = _payingDebt.value ?: return
        viewModelScope.launch {
            try {
                debtRepository.recordPayment(debt.id, amountVal)
                _isRecordPaymentDialogOpen.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to record payment."
            }
        }
    }

    fun onDeleteDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
        }
    }

    class Factory(
        private val debtRepository: DebtRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DebtViewModel(debtRepository, settingsRepository) as T
        }
    }
}
