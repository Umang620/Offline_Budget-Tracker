package com.example.magtipidka.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.magtipidka.di.AppContainer
import com.example.magtipidka.presentation.budget.BudgetScreen
import com.example.magtipidka.presentation.budget.BudgetViewModel
import com.example.magtipidka.presentation.dashboard.DashboardScreen
import com.example.magtipidka.presentation.dashboard.DashboardViewModel
import com.example.magtipidka.presentation.debt.DebtScreen
import com.example.magtipidka.presentation.debt.DebtViewModel
import com.example.magtipidka.presentation.pin.PinScreen
import com.example.magtipidka.presentation.pin.PinViewModel
import com.example.magtipidka.presentation.recurring.RecurringBillsScreen
import com.example.magtipidka.presentation.recurring.RecurringBillsViewModel
import com.example.magtipidka.presentation.reports.ReportsScreen
import com.example.magtipidka.presentation.reports.ReportsViewModel
import com.example.magtipidka.presentation.savings.SavingsGoalsScreen
import com.example.magtipidka.presentation.savings.SavingsViewModel
import com.example.magtipidka.presentation.settings.LicenseScreen
import com.example.magtipidka.presentation.settings.SettingsScreen
import com.example.magtipidka.presentation.settings.SettingsViewModel
import com.example.magtipidka.presentation.splash.SplashScreen
import com.example.magtipidka.presentation.transactions.AddEditTransactionScreen
import com.example.magtipidka.presentation.transactions.AddEditTransactionViewModel
import com.example.magtipidka.presentation.transactions.TransactionsScreen
import com.example.magtipidka.presentation.transactions.TransactionsViewModel

@Composable
fun MainNavGraph(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in BottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(350)) + slideInHorizontally(initialOffsetX = { 200 }, animationSpec = tween(350)) },
            exitTransition = { fadeOut(animationSpec = tween(350)) + slideOutHorizontally(targetOffsetX = { -200 }, animationSpec = tween(350)) },
            popEnterTransition = { fadeIn(animationSpec = tween(350)) + slideInHorizontally(initialOffsetX = { -200 }, animationSpec = tween(350)) },
            popExitTransition = { fadeOut(animationSpec = tween(350)) + slideOutHorizontally(targetOffsetX = { 200 }, animationSpec = tween(350)) }
        ) {
            // Splash Intro Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Pin.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Security PIN Screen
            composable(Screen.Pin.route) {
                val pinViewModel: PinViewModel = viewModel(
                    factory = PinViewModel.Factory(container.settingsRepository)
                )
                PinScreen(
                    viewModel = pinViewModel,
                    onAuthenticated = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Pin.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Screen
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(
                        container.calculateBalanceUseCase,
                        container.calculateTotalIncomeUseCase,
                        container.calculateTotalExpensesUseCase,
                        container.calculateBudgetProgressUseCase,
                        container.calculateDailySpendLimitUseCase,
                        container.calculateTipidScoreUseCase,
                        container.processDueRecurringBillsUseCase,
                        container.getTransactionsUseCase,
                        container.savingsGoalRepository,
                        container.debtRepository,
                        container.recurringBillRepository,
                        container.categoryRepository,
                        container.settingsRepository
                    )
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                    onNavigateToSavings = { navController.navigate(Screen.SavingsGoals.route) },
                    onNavigateToDebts = { navController.navigate(Screen.Debts.route) },
                    onNavigateToRecurringBills = { navController.navigate(Screen.RecurringBills.route) }
                )
            }

            // Transactions History Screen
            composable(Screen.Transactions.route) {
                val transactionsViewModel: TransactionsViewModel = viewModel(
                    factory = TransactionsViewModel.Factory(
                        container.getTransactionsUseCase,
                        container.deleteTransactionUseCase,
                        container.categoryRepository,
                        container.settingsRepository
                    )
                )
                TransactionsScreen(
                    viewModel = transactionsViewModel,
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToEditTransaction = { transactionId ->
                        navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                    }
                )
            }

            // Add Transaction Screen
            composable(Screen.AddTransaction.route) {
                val addTransactionViewModel: AddEditTransactionViewModel = viewModel(
                    factory = AddEditTransactionViewModel.Factory(
                        0L,
                        container.addTransactionUseCase,
                        container.editTransactionUseCase,
                        container.getTransactionByIdUseCase,
                        container.categoryRepository,
                        container.settingsRepository
                    )
                )
                AddEditTransactionScreen(
                    viewModel = addTransactionViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Transaction Screen
            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
                val editTransactionViewModel: AddEditTransactionViewModel = viewModel(
                    factory = AddEditTransactionViewModel.Factory(
                        transactionId,
                        container.addTransactionUseCase,
                        container.editTransactionUseCase,
                        container.getTransactionByIdUseCase,
                        container.categoryRepository,
                        container.settingsRepository
                    )
                )
                AddEditTransactionScreen(
                    viewModel = editTransactionViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Budget Planner Screen
            composable(Screen.Budget.route) {
                val budgetViewModel: BudgetViewModel = viewModel(
                    factory = BudgetViewModel.Factory(
                        container.setBudgetUseCase,
                        container.deleteBudgetUseCase,
                        container.calculateBudgetProgressUseCase,
                        container.categoryRepository,
                        container.settingsRepository,
                        container.transactionRepository
                    )
                )
                BudgetScreen(viewModel = budgetViewModel)
            }

            // Savings Goals Screen
            composable(Screen.SavingsGoals.route) {
                val savingsViewModel: SavingsViewModel = viewModel(
                    factory = SavingsViewModel.Factory(
                        container.savingsGoalRepository,
                        container.addSavingsGoalUseCase,
                        container.updateSavingsGoalUseCase,
                        container.deleteSavingsGoalUseCase,
                        container.calculateSavingsProgressUseCase,
                        container.settingsRepository
                    )
                )
                SavingsGoalsScreen(viewModel = savingsViewModel)
            }

            // Utang & Pautang (IOU Tracker) Screen
            composable(Screen.Debts.route) {
                val debtViewModel: DebtViewModel = viewModel(
                    factory = DebtViewModel.Factory(
                        container.debtRepository,
                        container.settingsRepository
                    )
                )
                DebtScreen(
                    viewModel = debtViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Subscriptions & Recurring Bills Screen
            composable(Screen.RecurringBills.route) {
                val recurringViewModel: RecurringBillsViewModel = viewModel(
                    factory = RecurringBillsViewModel.Factory(
                        container.recurringBillRepository,
                        container.categoryRepository,
                        container.settingsRepository
                    )
                )
                RecurringBillsScreen(
                    viewModel = recurringViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Financial Reports Screen
            composable(Screen.Reports.route) {
                val reportsViewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(
                        container.generateMonthlyReportUseCase,
                        container.settingsRepository
                    )
                )
                ReportsScreen(viewModel = reportsViewModel)
            }

            // Settings & Offline Data Screen
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.settingsRepository,
                        container.dataBackupRepository
                    )
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToSavingsGoals = { navController.navigate(Screen.SavingsGoals.route) },
                    onNavigateToDebts = { navController.navigate(Screen.Debts.route) },
                    onNavigateToRecurringBills = { navController.navigate(Screen.RecurringBills.route) },
                    onNavigateToLicense = { navController.navigate(Screen.License.route) }
                )
            }

            // About & License Screen
            composable(Screen.License.route) {
                LicenseScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
