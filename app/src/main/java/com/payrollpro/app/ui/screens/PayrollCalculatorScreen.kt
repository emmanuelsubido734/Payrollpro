package com.payrollpro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollCalculatorScreen(
    employee: Employee,
    onBack: () -> Unit,
    onCompute: (hoursWorked: Double, overtimeHours: Double) -> PayrollResult,
    onComputed: (PayrollResult) -> Unit
) {
    var hoursWorked by remember { mutableStateOf("") }
    var overtimeHours by remember { mutableStateOf("0") }
    var isComputing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(employee.fullName, style = MaterialTheme.typography.titleLarge)
            Text(
                "${employee.position} \u00b7 \u20b1${employee.hourlyRate}/hr",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = hoursWorked,
                onValueChange = { hoursWorked = it },
                label = { Text("Hours Worked") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = overtimeHours,
                onValueChange = { overtimeHours = it },
                label = { Text("Overtime Hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    isComputing = true
                    // TODO: this will call the SOAP transactions in sequence:
                    // ComputeGrossPay() -> ComputeTax() -> ComputeDeductions() -> ComputeNetSalary()
                    val result = onCompute(
                        hoursWorked.toDoubleOrNull() ?: 0.0,
                        overtimeHours.toDoubleOrNull() ?: 0.0
                    )
                    isComputing = false
                    onComputed(result)
                },
                enabled = hoursWorked.isNotBlank() && !isComputing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isComputing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Compute Payroll")
                }
            }
        }
    }
}
