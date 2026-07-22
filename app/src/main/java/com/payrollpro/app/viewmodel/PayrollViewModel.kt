package com.payrollpro.app.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.payrollpro.app.data.PayrollPreferences
import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult
import com.payrollpro.app.network.PayrollSoapClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PayrollViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PayrollPreferences(application)

    // Mock employee data — replace with records pulled from the database once the
    // REST/SOAP layer is wired in.
    val employees: SnapshotStateList<Employee> = mutableStateListOf(
        Employee("E001", "Juan", "Dela Cruz", "Machine Operator", 85.0, "single"),
        Employee("E002", "Maria", "Santos", "Line Supervisor", 120.0, "married"),
        Employee("E003", "Pedro", "Reyes", "Warehouse Staff", 75.0, "single")
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

    // Loaded from DataStore in init{} below; this is just the value shown before that load completes.
    var soapEndpoint = mutableStateOf(PayrollPreferences.DEFAULT_SOAP_ENDPOINT)
        private set

    fun setSoapEndpoint(url: String) {
        soapEndpoint.value = url
        viewModelScope.launch { preferences.saveSoapEndpoint(url) }
    }

    // Payroll defaults — editable from Settings, persisted via DataStore so they
    // survive app restarts, not just configuration changes.
    var overtimeMultiplier = mutableStateOf(PayrollPreferences.DEFAULT_OVERTIME_MULTIPLIER)
        private set
    var sss = mutableStateOf(PayrollPreferences.DEFAULT_SSS)
        private set
    var philHealth = mutableStateOf(PayrollPreferences.DEFAULT_PHILHEALTH)
        private set
    var pagIbig = mutableStateOf(PayrollPreferences.DEFAULT_PAGIBIG)
        private set
    var otherDeductions = mutableStateOf(PayrollPreferences.DEFAULT_OTHER_DEDUCTIONS)
        private set

    fun setOvertimeMultiplier(value: Double) {
        overtimeMultiplier.value = value
        viewModelScope.launch { preferences.saveOvertimeMultiplier(value) }
    }

    fun setSss(value: Double) {
        sss.value = value
        viewModelScope.launch { preferences.saveSss(value) }
    }

    fun setPhilHealth(value: Double) {
        philHealth.value = value
        viewModelScope.launch { preferences.savePhilHealth(value) }
    }

    fun setPagIbig(value: Double) {
        pagIbig.value = value
        viewModelScope.launch { preferences.savePagIbig(value) }
    }

    fun setOtherDeductions(value: Double) {
        otherDeductions.value = value
        viewModelScope.launch { preferences.saveOtherDeductions(value) }
    }

    init {
        viewModelScope.launch {
            soapEndpoint.value = preferences.loadSoapEndpoint()
            overtimeMultiplier.value = preferences.loadOvertimeMultiplier()
            sss.value = preferences.loadSss()
            philHealth.value = preferences.loadPhilHealth()
            pagIbig.value = preferences.loadPagIbig()
            otherDeductions.value = preferences.loadOtherDeductions()
        }
    }

    fun findEmployee(employeeId: String): Employee? =
        employees.find { it.employeeId == employeeId }

    fun addEmployee(employee: Employee) {
        employees.add(employee)
    }

    /**
     * Calls the four PHP SOAP transactions in sequence via PayrollSoapClient,
     * off the main thread, then delivers the result (or an error) back to the caller.
     * Overtime multiplier and deduction amounts come from the persisted Settings
     * defaults (overtimeMultiplier/sss/philHealth/pagIbig/otherDeductions above)
     * instead of being hardcoded here.
     */
    fun computePayroll(
        employee: Employee,
        hoursWorked: Double,
        overtimeHours: Double,
        onSuccess: (PayrollResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val client = PayrollSoapClient(soapEndpoint.value)

                    val gross = client.computeGrossPay(
                        employee.hourlyRate, hoursWorked, overtimeHours, overtimeMultiplier.value
                    )
                    val tax = client.computeTax(gross.grossPay, employee.civilStatus)
                    val deductions = client.computeDeductions(
                        sss.value, philHealth.value, pagIbig.value, otherDeductions.value
                    )
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