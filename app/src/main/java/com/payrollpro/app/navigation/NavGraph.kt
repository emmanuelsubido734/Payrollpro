package com.payrollpro.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.payrollpro.app.ui.screens.*
import com.payrollpro.app.viewmodel.PayrollViewModel

@Composable
fun PayrollNavGraph(navController: NavHostController) {
    val viewModel: PayrollViewModel = viewModel()

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
                onBack = { navController.popBackStack() },
                onAddEmployee = { navController.navigate(Screen.AddEmployee.route) },
                onSelectEmployee = { employee ->
                    navController.navigate(Screen.PayrollCalculator.createRoute(employee.employeeId))
                }
            )
        }

        composable(Screen.AddEmployee.route) {
            AddEmployeeScreen(
                onBack = { navController.popBackStack() },
                onSave = { employee -> viewModel.addEmployee(employee) }
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
                    onCompute = { hoursWorked, overtimeHours ->
                        viewModel.computePayroll(employee, hoursWorked, overtimeHours)
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
                    onBack = { navController.popBackStack(Screen.Dashboard.route, inclusive = false) }
                )
            }
        }

        composable(Screen.PayrollHistory.route) {
            PayrollHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
