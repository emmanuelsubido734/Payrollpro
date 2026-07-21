package com.payrollpro.app.model

data class PayrollResult(
    val employeeId: String,
    val hoursWorked: Double,
    val overtimeHours: Double,
    val grossPay: Double,
    val tax: Double,
    val deductions: Double,
    val netPay: Double,
    val date: String
)
