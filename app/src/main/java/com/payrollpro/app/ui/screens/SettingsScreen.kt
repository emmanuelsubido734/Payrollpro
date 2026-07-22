package com.payrollpro.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    soapEndpoint: String,
    onSoapEndpointChange: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEndpointSavedDialog by remember { mutableStateOf(false) }
    var endpointText by remember(soapEndpoint) { mutableStateOf(soapEndpoint) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            ListItem(
                headlineContent = { Text("Dark Mode") },
                trailingContent = {
                    Switch(checked = isDarkTheme, onCheckedChange = onDarkThemeChange)
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text("About") },
                modifier = Modifier.clickable { showAboutDialog = true }
            )
            Divider()

            Column(modifier = Modifier.padding(16.dp)) {
                Text("SOAP Endpoint", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = endpointText,
                    onValueChange = { endpointText = it },
                    label = { Text("Endpoint URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSoapEndpointChange(endpointText)
                        showEndpointSavedDialog = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }
            Divider()

            ListItem(
                headlineContent = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { onLogout() }
            )
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            },
            title = { Text("About PayrollPro") },
            text = { Text("PayrollPro \u2014 Android Payroll Management System\nIT130 Machine Problem") }
        )
    }

    if (showEndpointSavedDialog) {
        AlertDialog(
            onDismissRequest = { showEndpointSavedDialog = false },
            confirmButton = {
                TextButton(onClick = { showEndpointSavedDialog = false }) { Text("OK") }
            },
            title = { Text("Endpoint Updated") },
            text = { Text("SOAP endpoint has been changed to:\n$endpointText") }
        )
    }
}