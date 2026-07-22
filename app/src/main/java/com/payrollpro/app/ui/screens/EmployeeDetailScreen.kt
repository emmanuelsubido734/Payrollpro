package com.payrollpro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payrollpro.app.model.Employee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employee: Employee,
    onBack: () -> Unit,
    onComputePayroll: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Details") },
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
            DetailRow("Employee ID", employee.employeeId)
            DetailRow("Full Name", employee.fullName)
            DetailRow("Position", employee.position)
            DetailRow("Hourly Rate", "\u20b1${employee.hourlyRate}")
            DetailRow("Civil Status", employee.civilStatus.replaceFirstChar { it.uppercase() })

            Spacer(Modifier.height(24.dp))
            Button(onClick = onComputePayroll, modifier = Modifier.fillMaxWidth()) {
                Text("Compute Payroll")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
    Divider()
}