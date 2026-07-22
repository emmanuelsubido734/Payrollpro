package com.payrollpro.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult
import com.payrollpro.app.network.PayrollSoapClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Every confirmed payroll gets appended here — this is what the History screen reads.
    // TODO: once the backend exists, load this from the Payroll table instead of memory.
    val payrollHistory: SnapshotStateList<PayrollResult> = mutableStateListOf()

    var isDarkTheme = mutableStateOf(false)
        private set

    fun setDarkTheme(enabled: Boolean) {
        isDarkTheme.value = enabled
    }

    var soapEndpoint = mutableStateOf("http://10.0.2.2/payroll/soap_server.php")
        private set

    fun setSoapEndpoint(url: String) {
        soapEndpoint.value = url
    }

    fun findEmployee(employeeId: String): Employee? =
        employees.find { it.employeeId == employeeId }

    fun addEmployee(employee: Employee) {
        employees.add(employee)
    }

    /**
     * Calls the four PHP SOAP transactions in sequence via PayrollSoapClient,
     * off the main thread, then delivers the result (or an error) back to the caller.
     */
    fun computePayroll(
        employee: Employee,
        hoursWorked: Double,
        overtimeHours: Double,
        overtimeMultiplier: Double = 1.25,
        civilStatus: String = "single",
        sss: Double = 500.0,
        philHealth: Double = 250.0,
        pagIbig: Double = 100.0,
        otherDeductions: Double = 0.0,
        onSuccess: (PayrollResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val client = PayrollSoapClient(soapEndpoint.value)

                    val gross = client.computeGrossPay(
                        employee.hourlyRate, hoursWorked, overtimeHours, overtimeMultiplier
                    )
                    val tax = client.computeTax(gross.grossPay, civilStatus)
                    val deductions = client.computeDeductions(sss, philHealth, pagIbig, otherDeductions)
                    val netPay = client.computeNetSalary(gross.grossPay, tax, deductions)

                    PayrollResult(
                        employeeId = employee.employeeId,
                        hoursWorked = hoursWorked,
                        overtimeHours = overtimeHours,
                        grossPay = gross.grossPay,
                        tax = tax,
                        deductions = deductions,
                        netPay = netPay,
                        date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                    )
                }
                lastResult.value = result
                onSuccess(result)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to reach the payroll server.")
            }
        }
    }

    /**
     * Commits a previewed PayrollResult to history. Called only when the
     * user explicitly confirms the payslip, not automatically on compute.
     */
    fun confirmPayroll(result: PayrollResult) {
        payrollHistory.add(0, result) // newest first
    }
}