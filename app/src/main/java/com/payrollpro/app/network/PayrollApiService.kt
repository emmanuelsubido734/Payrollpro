package com.payrollpro.app.network

import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Query

interface PayrollApiService {
    @GET("employees.php")
    suspend fun getEmployees(): List<Employee>

    @POST("employees.php")
    suspend fun addEmployee(@Body employee: Employee)

    @DELETE("employees.php")
    suspend fun deleteEmployee(@Query("id") employeeId: String)

    @POST("payroll.php")
    suspend fun submitPayroll(@Body result: PayrollResult)

    @GET("payroll.php")
    suspend fun getPayrollHistory(@Query("employee_id") employeeId: String? = null): List<PayrollResult>
}