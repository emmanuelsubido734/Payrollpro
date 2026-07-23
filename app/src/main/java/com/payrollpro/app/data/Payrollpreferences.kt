package com.payrollpro.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.booleanPreferencesKey

private val Context.payrollDataStore by preferencesDataStore(name = "payroll_settings")

/**
 * Wraps Jetpack DataStore so the SOAP endpoint and payroll deduction defaults
 * survive process death / app restarts, not just configuration changes
 * (which plain ViewModel state already survives on its own).
 */
class PayrollPreferences(private val context: Context) {

    private object Keys {
        val SOAP_ENDPOINT = stringPreferencesKey("soap_endpoint")
        val REST_BASE_URL = stringPreferencesKey("rest_base_url")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val OVERTIME_MULTIPLIER = doublePreferencesKey("overtime_multiplier")
        val SSS = doublePreferencesKey("sss")
        val PHILHEALTH = doublePreferencesKey("philhealth")
        val PAGIBIG = doublePreferencesKey("pagibig")
        val OTHER_DEDUCTIONS = doublePreferencesKey("other_deductions")
    }

    companion object {
        const val DEFAULT_SOAP_ENDPOINT = "http://10.0.2.2/payroll/soap_server.php"
        const val DEFAULT_REST_BASE_URL = "http://10.0.2.2/payroll/"
        const val DEFAULT_DARK_THEME = false
        const val DEFAULT_OVERTIME_MULTIPLIER = 1.25
        const val DEFAULT_SSS = 500.0
        const val DEFAULT_PHILHEALTH = 250.0
        const val DEFAULT_PAGIBIG = 100.0
        const val DEFAULT_OTHER_DEDUCTIONS = 0.0
    }

    suspend fun loadDarkTheme(): Boolean =
        context.payrollDataStore.data.first()[Keys.DARK_THEME] ?: DEFAULT_DARK_THEME

    suspend fun saveDarkTheme(value: Boolean) {
        context.payrollDataStore.edit { it[Keys.DARK_THEME] = value }
    }

    suspend fun loadSoapEndpoint(): String =
        context.payrollDataStore.data.first()[Keys.SOAP_ENDPOINT] ?: DEFAULT_SOAP_ENDPOINT

    suspend fun saveSoapEndpoint(value: String) {
        context.payrollDataStore.edit { it[Keys.SOAP_ENDPOINT] = value }
    }

    suspend fun loadRestBaseUrl(): String =
        context.payrollDataStore.data.first()[Keys.REST_BASE_URL] ?: DEFAULT_REST_BASE_URL

    suspend fun saveRestBaseUrl(value: String) {
        context.payrollDataStore.edit { it[Keys.REST_BASE_URL] = value }
    }

    suspend fun loadOvertimeMultiplier(): Double =
        context.payrollDataStore.data.first()[Keys.OVERTIME_MULTIPLIER] ?: DEFAULT_OVERTIME_MULTIPLIER

    suspend fun saveOvertimeMultiplier(value: Double) {
        context.payrollDataStore.edit { it[Keys.OVERTIME_MULTIPLIER] = value }
    }

    suspend fun loadSss(): Double =
        context.payrollDataStore.data.first()[Keys.SSS] ?: DEFAULT_SSS

    suspend fun saveSss(value: Double) {
        context.payrollDataStore.edit { it[Keys.SSS] = value }
    }

    suspend fun loadPhilHealth(): Double =
        context.payrollDataStore.data.first()[Keys.PHILHEALTH] ?: DEFAULT_PHILHEALTH

    suspend fun savePhilHealth(value: Double) {
        context.payrollDataStore.edit { it[Keys.PHILHEALTH] = value }
    }

    suspend fun loadPagIbig(): Double =
        context.payrollDataStore.data.first()[Keys.PAGIBIG] ?: DEFAULT_PAGIBIG

    suspend fun savePagIbig(value: Double) {
        context.payrollDataStore.edit { it[Keys.PAGIBIG] = value }
    }

    suspend fun loadOtherDeductions(): Double =
        context.payrollDataStore.data.first()[Keys.OTHER_DEDUCTIONS] ?: DEFAULT_OTHER_DEDUCTIONS

    suspend fun saveOtherDeductions(value: Double) {
        context.payrollDataStore.edit { it[Keys.OTHER_DEDUCTIONS] = value }
    }
}