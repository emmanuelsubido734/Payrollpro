package com.payrollpro.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.payrollpro.app.ui.screens.*
import com.payrollpro.app.viewmodel.PayrollViewModel

@Composable
fun PayrollNavGraph(navController: NavHostController, viewModel: PayrollViewModel) {

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateEmployees = { navController.navigate(Screen.EmployeeList.route) },
                onNavigateCalculator = { navController.navigate(Screen.EmployeeList.route) },
                onNavigateHistory = { navController.navigate(Screen.PayrollHistory.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.EmployeeList.route) {
            EmployeeListScreen(
                employees = viewModel.employees,
                isLoading = viewModel.isLoadingEmployees.value,
                errorMessage = viewModel.employeesError.value,
                onBack = { navController.popBackStack() },
                onAddEmployee = { navController.navigate(Screen.AddEmployee.route) },
                onSelectEmployee = { employee ->
                    navController.navigate(Screen.PayrollCalculator.createRoute(employee.employeeId))
                },
                onRefresh = { viewModel.loadEmployeesFromServer() },
                onDeleteEmployee = { employee, onSuccess, onError ->
                    viewModel.deleteEmployee(employee.employeeId, onSuccess, onError)
                }
            )
        }

        composable(Screen.AddEmployee.route) {
            AddEmployeeScreen(
                isSaving = viewModel.isSavingEmployee.value,
                onBack = { navController.popBackStack() },
                onSave = { employee, onSuccess, onError ->
                    viewModel.addEmployeeRemote(employee, onSuccess, onError)
                }
            )
        }

        composable(
            route = Screen.PayrollCalculator.route,
            arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val employeeId = backStackEntry.arguments?.getString("employeeId") ?: ""
            val employee = viewModel.findEmployee(employeeId)
            if (employee != null) {
                PayrollCalculatorScreen(
                    employee = employee,
                    onBack = { navController.popBackStack() },
                    onCompute = { hoursWorked, overtimeHours, onResult, onError ->
                        viewModel.computePayroll(
                            employee = employee,
                            hoursWorked = hoursWorked,
                            overtimeHours = overtimeHours,
                            onSuccess = onResult,
                            onError = onError
                        )
                    },
                    onComputed = { navController.navigate(Screen.Payslip.route) }
                )
            }
        }

        composable(Screen.Payslip.route) {
            val result = viewModel.lastResult.value
            val employee = result?.let { viewModel.findEmployee(it.employeeId) }
            if (result != null && employee != null) {
                PayslipScreen(
                    result = result,
                    employeeName = employee.fullName,
                    onBack = { navController.popBackStack(Screen.Dashboard.route, inclusive = false) },
                    onConfirm = { confirmedResult -> viewModel.confirmPayroll(confirmedResult) }
                )
            }
        }

        composable(Screen.PayrollHistory.route) {
            PayrollHistoryScreen(
                history = viewModel.payrollHistory,
                isLoading = viewModel.isLoadingHistory.value,
                errorMessage = viewModel.historyError.value,
                findEmployee = { id -> viewModel.findEmployee(id) },
                onBack = { navController.popBackStack() },
                onRefresh = { viewModel.loadPayrollHistory() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                isDarkTheme = viewModel.isDarkTheme.value,
                onDarkThemeChange = { viewModel.setDarkTheme(it) },
                soapEndpoint = viewModel.soapEndpoint.value,
                onSoapEndpointChange = { viewModel.setSoapEndpoint(it) },
                restBaseUrl = viewModel.restBaseUrl.value,
                onRestBaseUrlChange = { viewModel.setRestBaseUrl(it) },
                overtimeMultiplier = viewModel.overtimeMultiplier.value,
                onOvertimeMultiplierChange = { viewModel.setOvertimeMultiplier(it) },
                sss = viewModel.sss.value,
                onSssChange = { viewModel.setSss(it) },
                philHealth = viewModel.philHealth.value,
                onPhilHealthChange = { viewModel.setPhilHealth(it) },
                pagIbig = viewModel.pagIbig.value,
                onPagIbigChange = { viewModel.setPagIbig(it) },
                otherDeductions = viewModel.otherDeductions.value,
                onOtherDeductionsChange = { viewModel.setOtherDeductions(it) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}