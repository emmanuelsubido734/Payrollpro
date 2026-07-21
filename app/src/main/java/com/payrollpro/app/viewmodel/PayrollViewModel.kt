package com.payrollpro.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult

class PayrollViewModel : ViewModel() {

    // Mock employee data — replace with records pulled from the database once the
    // REST/SOAP layer is wired in.
    val employees: SnapshotStateList<Employee> = mutableStateListOf(
        Employee("E001", "Juan", "Dela Cruz", "Machine Operator", 85.0),
        Employee("E002", "Maria", "Santos", "Line Supervisor", 120.0),
        Employee("E003", "Pedro", "Reyes", "Warehouse Staff", 75.0)
    )

    var lastResult = mutableStateOf<PayrollResult?>(null)
        private set

    fun findEmployee(employeeId: String): Employee? =
        employees.find { it.employeeId == employeeId }

    fun addEmployee(employee: Employee) {
        employees.add(employee)
    }

    /**
     * Local placeholder so the UI is testable before the SOAP service exists.
     * TODO: replace this with sequential ksoap2-android calls to the PHP SOAP server:
     *   1. ComputeGrossPay(hourlyRate, hoursWorked, overtimeHours, overtimeMultiplier)
     *   2. ComputeTax(grossPay, taxRate, civilStatus)
     *   3. ComputeDeductions(grossPay, sss, philHealth, pagIbig, otherDeductions)
     *   4. ComputeNetSalary(grossPay, tax, deductions)
     */
    fun computePayroll(
        employee: Employee,
        hoursWorked: Double,
        overtimeHours: Double,
        overtimeMultiplier: Double = 1.25,
        taxRate: Double = 0.10,
        sss: Double = 500.0,
        philHealth: Double = 250.0,
        pagIbig: Double = 100.0,
        otherDeductions: Double = 0.0
    ): PayrollResult {
        val regularPay = hoursWorked * employee.hourlyRate
        val overtimePay = overtimeHours * employee.hourlyRate * overtimeMultiplier
        val grossPay = regularPay + overtimePay
        val tax = grossPay * taxRate
        val deductions = sss + philHealth + pagIbig + otherDeductions
        val netPay = grossPay - tax - deductions

        val result = PayrollResult(
            employeeId = employee.employeeId,
            hoursWorked = hoursWorked,
            overtimeHours = overtimeHours,
            grossPay = grossPay,
            tax = tax,
            deductions = deductions,
            netPay = netPay,
            date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        )
        lastResult.value = result
        return result
    }
}
