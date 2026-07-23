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
import com.payrollpro.app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PayrollViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PayrollPreferences(application)

    // Populated from the server via loadEmployeesFromServer() once preferences
    // finish loading in init{}. Starts empty; the UI should show a loading
    // state until the first fetch completes.
    val employees: SnapshotStateList<Employee> = mutableStateListOf()

    var isLoadingEmployees = mutableStateOf(false)
        private set
    var employeesError = mutableStateOf<String?>(null)
        private set

    var isSavingEmployee = mutableStateOf(false)
        private set

    var lastResult = mutableStateOf<PayrollResult?>(null)
        private set

    // Loaded from the server via loadPayrollHistory(). Also appended to
    // locally (optimistically) whenever confirmPayroll() is called.
    val payrollHistory: SnapshotStateList<PayrollResult> = mutableStateListOf()

    var isLoadingHistory = mutableStateOf(false)
        private set
    var historyError = mutableStateOf<String?>(null)
        private set

    var isDarkTheme = mutableStateOf(false)
        private set

    fun setDarkTheme(enabled: Boolean) {
        isDarkTheme.value = enabled
        viewModelScope.launch { preferences.saveDarkTheme(enabled) }
    }

    // Loaded from DataStore in init{} below; this is just the value shown before that load completes.
    var soapEndpoint = mutableStateOf(PayrollPreferences.DEFAULT_SOAP_ENDPOINT)
        private set

    fun setSoapEndpoint(url: String) {
        soapEndpoint.value = url
        viewModelScope.launch { preferences.saveSoapEndpoint(url) }
    }

    // REST API base URL (for employees.php / payroll.php), separate from the
    // SOAP endpoint above. Editable from Settings, persisted via DataStore.
    var restBaseUrl = mutableStateOf(PayrollPreferences.DEFAULT_REST_BASE_URL)
        private set

    fun setRestBaseUrl(url: String) {
        restBaseUrl.value = url
        RetrofitClient.baseUrl = url
        viewModelScope.launch { preferences.saveRestBaseUrl(url) }
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
    var isDeletingEmployee = mutableStateOf(false)
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
            restBaseUrl.value = preferences.loadRestBaseUrl()
            RetrofitClient.baseUrl = restBaseUrl.value
            isDarkTheme.value = preferences.loadDarkTheme()
            overtimeMultiplier.value = preferences.loadOvertimeMultiplier()
            sss.value = preferences.loadSss()
            philHealth.value = preferences.loadPhilHealth()
            pagIbig.value = preferences.loadPagIbig()
            otherDeductions.value = preferences.loadOtherDeductions()

            loadEmployeesFromServer()
            loadPayrollHistory()
        }
    }

    fun findEmployee(employeeId: String): Employee? =
        employees.find { it.employeeId == employeeId }

    /**
     * Fetches the employee list from employees.php (GET). Call this on
     * screen load and whenever the user pulls to refresh.
     */
    fun loadEmployeesFromServer() {
        viewModelScope.launch {
            isLoadingEmployees.value = true
            employeesError.value = null
            try {
                val remote = withContext(Dispatchers.IO) { RetrofitClient.api.getEmployees() }
                employees.clear()
                employees.addAll(remote)
            } catch (e: Exception) {
                employeesError.value = e.message ?: "Could not reach the server."
            } finally {
                isLoadingEmployees.value = false
            }
        }
    }

    /**
     * Posts a new employee to employees.php (POST). Adds it to the local
     * list only after the server confirms the insert succeeded.
     */
    fun addEmployeeRemote(
        employee: Employee,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSavingEmployee.value = true
            try {
                withContext(Dispatchers.IO) { RetrofitClient.api.addEmployee(employee) }
                employees.add(employee)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save employee.")
            } finally {
                isSavingEmployee.value = false
            }
        }
    }
    /**
     * Deletes an employee on the server (employees.php DELETE), then removes
     * it locally only after the server confirms. Fails with a clear message
     * if the employee has existing payroll records (FK constraint).
     */
    fun deleteEmployee(
        employeeId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isDeletingEmployee.value = true
            try {
                withContext(Dispatchers.IO) { RetrofitClient.api.deleteEmployee(employeeId) }
                employees.removeAll { it.employeeId == employeeId }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete employee.")
            } finally {
                isDeletingEmployee.value = false
            }
        }
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
     * Commits a previewed PayrollResult to history: adds it locally right away
     * (optimistic update) and syncs it to payroll.php in the background. If the
     * sync fails, the record stays in local history but historyError is set so
     * the UI can flag it as unsynced.
     */
    fun confirmPayroll(result: PayrollResult) {
        payrollHistory.add(0, result) // newest first
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { RetrofitClient.api.submitPayroll(result) }
            } catch (e: Exception) {
                historyError.value = "Saved locally, but couldn't sync to server: ${e.message}"
            }
        }
    }

    /**
     * Fetches payroll history from payroll.php (GET). Call this on screen
     * load and whenever the user pulls to refresh.
     */
    fun loadPayrollHistory() {
        viewModelScope.launch {
            isLoadingHistory.value = true
            historyError.value = null
            try {
                val remote = withContext(Dispatchers.IO) { RetrofitClient.api.getPayrollHistory() }
                payrollHistory.clear()
                payrollHistory.addAll(remote)
            } catch (e: Exception) {
                historyError.value = e.message ?: "Could not reach the server."
            } finally {
                isLoadingHistory.value = false
            }
        }
    }
}