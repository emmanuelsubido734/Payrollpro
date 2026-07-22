package com.payrollpro.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollHistoryScreen(
    history: List<PayrollResult>,
    isLoading: Boolean,
    errorMessage: String?,
    findEmployee: (String) -> Employee?,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    var selectedRecord by remember { mutableStateOf<PayrollResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            errorMessage?.let {
                Text(
                    it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
            if (history.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No payroll records yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(history) { record ->
                        val employeeName = findEmployee(record.employeeId)?.fullName ?: record.employeeId
                        ListItem(
                            headlineContent = { Text(employeeName) },
                            supportingContent = { Text("ID: ${record.employeeId} \u00b7 ${record.date}") },
                            trailingContent = {
                                Text("\u20b1${String.format(Locale.US, "%.2f", record.netPay)}")
                            },
                            modifier = Modifier.clickable { selectedRecord = record }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    selectedRecord?.let { record ->
        val employeeName = findEmployee(record.employeeId)?.fullName ?: record.employeeId
        AlertDialog(
            onDismissRequest = { selectedRecord = null },
            confirmButton = {
                TextButton(onClick = { selectedRecord = null }) { Text("Close") }
            },
            title = { Text("Payroll Details") },
            text = {
                Column {
                    Text("Employee: $employeeName")
                    Text("Employee ID: ${record.employeeId}")
                    Text("Date: ${record.date}")
                    Spacer(Modifier.height(8.dp))
                    Text("Hours Worked: ${record.hoursWorked}")
                    Text("Overtime Hours: ${record.overtimeHours}")
                    Spacer(Modifier.height(8.dp))
                    Text("Gross Pay: \u20b1${String.format(Locale.US, "%.2f", record.grossPay)}")
                    Text("Tax: \u20b1${String.format(Locale.US, "%.2f", record.tax)}")
                    Text("Deductions: \u20b1${String.format(Locale.US, "%.2f", record.deductions)}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Net Pay: \u20b1${String.format(Locale.US, "%.2f", record.netPay)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        )
    }
}