package com.example.magtipidka.presentation.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magtipidka.domain.model.SavingsGoal
import com.example.magtipidka.domain.repository.SavingsGoalRepository
import com.example.magtipidka.domain.repository.SettingsRepository
import com.example.magtipidka.domain.usecase.savings.AddSavingsGoalUseCase
import com.example.magtipidka.domain.usecase.savings.CalculateSavingsProgressUseCase
import com.example.magtipidka.domain.usecase.savings.DeleteSavingsGoalUseCase
import com.example.magtipidka.domain.usecase.savings.UpdateSavingsGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SavingsViewModel(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val addSavingsGoalUseCase: AddSavingsGoalUseCase,
    private val updateSavingsGoalUseCase: UpdateSavingsGoalUseCase,
    private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase,
    private val calculateSavingsProgressUseCase: CalculateSavingsProgressUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isAddEditGoalDialogOpen = MutableStateFlow(false)
    private val _editingGoal = MutableStateFlow<SavingsGoal?>(null)
    private val _goalNameInput = MutableStateFlow("")
    private val _targetAmountInput = MutableStateFlow("")
    private val _currentAmountInput = MutableStateFlow("")
    private val _targetDateInput = MutableStateFlow(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))

    private val _isAddContributionDialogOpen = MutableStateFlow(false)
    private val _contributingGoal = MutableStateFlow<SavingsGoal?>(null)
    private val _contributionAmountInput = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        observeSavingsData()
    }

    private fun observeSavingsData() {
        val goalsProgressFlow = savingsGoalRepository.getAllSavingsGoals().combine(
            settingsRepository.getSettings()
        ) { goals, settings ->
            val progresses = goals.map { calculateSavingsProgressUseCase(it) }
            Pair(progresses, settings.currencySymbol)
        }

        val goalFieldsFlow = combine(
            _goalNameInput,
            _targetAmountInput,
            _currentAmountInput,
            _targetDateInput
        ) { name, target, current, date ->
            GoalFields(name, target, current, date)
        }

        val goalDialogStateFlow = combine(
            _isAddEditGoalDialogOpen,
            _editingGoal,
            goalFieldsFlow
        ) { open, editing, fields ->
            GoalDialogState(open, editing, fields.name, fields.target, fields.current, fields.date)
        }

        val contribStateFlow = combine(
            _isAddContributionDialogOpen,
            _contributingGoal,
            _contributionAmountInput
        ) { open, goal, amount ->
            ContribDialogState(open, goal, amount)
        }

        combine(
            goalsProgressFlow,
            goalDialogStateFlow,
            contribStateFlow,
            _errorMessage
        ) { goalsSettings, dialogState, contribState, error ->
            SavingsUiState(
                isLoading = false,
                currencySymbol = goalsSettings.second,
                savingsProgresses = goalsSettings.first,
                isAddEditGoalDialogOpen = dialogState.open,
                editingGoal = dialogState.editing,
                goalNameInput = dialogState.name,
                targetAmountInput = dialogState.target,
                currentAmountInput = dialogState.current,
                targetDateInput = dialogState.date,
                isAddContributionDialogOpen = contribState.open,
                contributingGoal = contribState.goal,
                contributionAmountInput = contribState.amount,
                errorMessage = error
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private data class GoalFields(
        val name: String,
        val target: String,
        val current: String,
        val date: Long
    )

    private data class GoalDialogState(
        val open: Boolean,
        val editing: SavingsGoal?,
        val name: String,
        val target: String,
        val current: String,
        val date: Long
    )

    private data class ContribDialogState(
        val open: Boolean,
        val goal: SavingsGoal?,
        val amount: String
    )

    fun onOpenAddGoalDialog(existingGoal: SavingsGoal? = null) {
        _editingGoal.value = existingGoal
        _goalNameInput.value = existingGoal?.name ?: ""
        _targetAmountInput.value = existingGoal?.targetAmount?.toString() ?: ""
        _currentAmountInput.value = existingGoal?.currentAmount?.toString() ?: "0.0"
        _targetDateInput.value = existingGoal?.targetDate ?: (System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
        _errorMessage.value = null
        _isAddEditGoalDialogOpen.value = true
    }

    fun onDismissGoalDialog() {
        _isAddEditGoalDialogOpen.value = false
    }

    fun onGoalNameChanged(name: String) { _goalNameInput.value = name; _errorMessage.value = null }
    fun onTargetAmountChanged(amount: String) { _targetAmountInput.value = amount; _errorMessage.value = null }
    fun onCurrentAmountChanged(amount: String) { _currentAmountInput.value = amount; _errorMessage.value = null }
    fun onTargetDateChanged(date: Long) { _targetDateInput.value = date }

    fun onSaveGoal() {
        val name = _goalNameInput.value.trim()
        if (name.isEmpty()) { _errorMessage.value = "Please enter a goal name."; return }

        val targetVal = _targetAmountInput.value.toDoubleOrNull()
        if (targetVal == null || targetVal <= 0) { _errorMessage.value = "Please enter a valid target amount."; return }

        val currentVal = _currentAmountInput.value.toDoubleOrNull() ?: 0.0

        val goal = SavingsGoal(
            id = _editingGoal.value?.id ?: 0L,
            name = name,
            targetAmount = targetVal,
            currentAmount = currentVal,
            targetDate = _targetDateInput.value
        )

        viewModelScope.launch {
            try {
                if (_editingGoal.value != null) {
                    updateSavingsGoalUseCase(goal)
                } else {
                    addSavingsGoalUseCase(goal)
                }
                _isAddEditGoalDialogOpen.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save goal."
            }
        }
    }

    fun onOpenAddContributionDialog(goal: SavingsGoal) {
        _contributingGoal.value = goal
        _contributionAmountInput.value = ""
        _errorMessage.value = null
        _isAddContributionDialogOpen.value = true
    }

    fun onDismissContributionDialog() {
        _isAddContributionDialogOpen.value = false
    }

    fun onContributionAmountChanged(amount: String) {
        _contributionAmountInput.value = amount
        _errorMessage.value = null
    }

    fun onSaveContribution() {
        val amountVal = _contributionAmountInput.value.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            _errorMessage.value = "Please enter a valid contribution amount."
            return
        }

        val goal = _contributingGoal.value ?: return
        viewModelScope.launch {
            try {
                updateSavingsGoalUseCase.addContribution(goal.id, amountVal)
                _isAddContributionDialogOpen.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add contribution."
            }
        }
    }

    fun onDeleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            deleteSavingsGoalUseCase(goal)
        }
    }

    class Factory(
        private val savingsGoalRepository: SavingsGoalRepository,
        private val addSavingsGoalUseCase: AddSavingsGoalUseCase,
        private val updateSavingsGoalUseCase: UpdateSavingsGoalUseCase,
        private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase,
        private val calculateSavingsProgressUseCase: CalculateSavingsProgressUseCase,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SavingsViewModel(
                savingsGoalRepository,
                addSavingsGoalUseCase,
                updateSavingsGoalUseCase,
                deleteSavingsGoalUseCase,
                calculateSavingsProgressUseCase,
                settingsRepository
            ) as T
        }
    }
}
