package com.payrollpro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payrollpro.app.model.PayrollResult
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(
    result: PayrollResult,
    employeeName: String,
    onBack: () -> Unit,
    onConfirm: (PayrollResult) -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payslip") },
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
            Text(employeeName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(result.date, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Divider()
            PayslipRow("Hours Worked", "${result.hoursWorked}")
            PayslipRow("Overtime Hours", "${result.overtimeHours}")
            PayslipRow("Gross Pay", "\u20b1${String.format(Locale.US, "%.2f", result.grossPay)}")
            PayslipRow("Tax", "\u20b1${String.format(Locale.US, "%.2f", result.tax)}")
            PayslipRow("Deductions", "\u20b1${String.format(Locale.US, "%.2f", result.deductions)}")
            Divider()
            PayslipRow("Net Pay", "\u20b1${String.format(Locale.US, "%.2f", result.netPay)}", emphasize = true)

            Spacer(Modifier.height(24.dp))

            if (isConfirmed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saved to Payroll History")
                }
            } else {
                Button(
                    onClick = {
                        onConfirm(result)
                        isConfirmed = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Payroll")
                }
            }
        }
    }
}

@Composable
private fun PayslipRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        )
    }
}