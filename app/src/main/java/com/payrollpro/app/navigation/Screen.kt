package com.payrollpro.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object EmployeeList : Screen("employee_list")
    object AddEmployee : Screen("add_employee")
    object PayrollCalculator : Screen("payroll_calculator/{employeeId}") {
        fun createRoute(employeeId: String) = "payroll_calculator/$employeeId"
    }
    object Payslip : Screen("payslip")
    object PayrollHistory : Screen("payroll_history")
    object Settings : Screen("settings")
}
