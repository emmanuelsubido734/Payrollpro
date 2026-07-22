package com.payrollpro.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.payrollpro.app.model.Employee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    employees: List<Employee>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onAddEmployee: () -> Unit,
    onSelectEmployee: (Employee) -> Unit,
    onRefresh: () -> Unit,
    onDeleteEmployee: (Employee, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employees") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEmployee) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            errorMessage?.let {
                Text(
                    "Couldn't load from server: $it",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
            deleteError?.let {
                Text(
                    it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
            if (employees.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No employees found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(employees, key = { it.employeeId }) { employee ->
                        ListItem(
                            headlineContent = { Text(employee.fullName) },
                            supportingContent = { Text("ID: ${employee.employeeId} \u00b7 ${employee.position} \u00b7 \u20b1${employee.hourlyRate}/hr") },
                            trailingContent = {
                                IconButton(onClick = {
                                    deleteError = null
                                    employeeToDelete = employee
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete ${employee.fullName}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onSelectEmployee(employee) }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    employeeToDelete?.let { employee ->
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Delete Employee") },
            text = { Text("Remove ${employee.fullName} (${employee.employeeId})? This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteEmployee(
                        employee,
                        { employeeToDelete = null },
                        { message ->
                            deleteError = message
                            employeeToDelete = null
                        }
                    )
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}