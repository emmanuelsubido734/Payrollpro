package com.payrollpro.app.model

data class Employee(
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val position: String,
    val hourlyRate: Double,
    val civilStatus: String = "single"
) {
    val fullName: String get() = "$firstName $lastName"
}
