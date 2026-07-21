package com.payrollpro.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payrollpro.app.model.Employee
import com.payrollpro.app.model.PayrollResult
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollHistoryScreen(
    history: List<PayrollResult>,
    findEmployee: (String) -> Employee?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No payroll records yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(history) { record ->
                    val employeeName = findEmployee(record.employeeId)?.fullName ?: record.employeeId
                    ListItem(
                        headlineContent = { Text(employeeName) },
                        supportingContent = { Text(record.date) },
                        trailingContent = {
                            Text("\u20b1${String.format(Locale.US, "%.2f", record.netPay)}")
                        }
                    )
                    Divider()
                }
            }
        }
    }
}