package com.payrollpro.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    soapEndpoint: String,
    onSoapEndpointChange: (String) -> Unit,
    overtimeMultiplier: Double,
    onOvertimeMultiplierChange: (Double) -> Unit,
    sss: Double,
    onSssChange: (Double) -> Unit,
    philHealth: Double,
    onPhilHealthChange: (Double) -> Unit,
    pagIbig: Double,
    onPagIbigChange: (Double) -> Unit,
    otherDeductions: Double,
    onOtherDeductionsChange: (Double) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    restBaseUrl: String,
    onRestBaseUrlChange: (String) -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEndpointSavedDialog by remember { mutableStateOf(false) }
    var showDefaultsSavedDialog by remember { mutableStateOf(false) }
    var endpointText by remember(soapEndpoint) { mutableStateOf(soapEndpoint) }
    var restUrlText by remember(restBaseUrl) { mutableStateOf(restBaseUrl) }
    var showRestUrlSavedDialog by remember { mutableStateOf(false) }
    var overtimeText by remember(overtimeMultiplier) { mutableStateOf(overtimeMultiplier.toString()) }
    var sssText by remember(sss) { mutableStateOf(sss.toString()) }
    var philHealthText by remember(philHealth) { mutableStateOf(philHealth.toString()) }
    var pagIbigText by remember(pagIbig) { mutableStateOf(pagIbig.toString()) }
    var otherDeductionsText by remember(otherDeductions) { mutableStateOf(otherDeductions.toString()) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

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

            Column(modifier = Modifier.padding(16.dp)) {
                Text("REST API Base URL", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = restUrlText,
                    onValueChange = { restUrlText = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onRestBaseUrlChange(restUrlText)
                        showRestUrlSavedDialog = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save")
                }
            }
            Divider()

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Payroll Defaults", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = overtimeText,
                    onValueChange = { overtimeText = it },
                    label = { Text("Overtime Multiplier") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = sssText,
                    onValueChange = { sssText = it },
                    label = { Text("SSS Deduction") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = philHealthText,
                    onValueChange = { philHealthText = it },
                    label = { Text("PhilHealth Deduction") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pagIbigText,
                    onValueChange = { pagIbigText = it },
                    label = { Text("Pag-IBIG Deduction") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = otherDeductionsText,
                    onValueChange = { otherDeductionsText = it },
                    label = { Text("Other Deductions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onOvertimeMultiplierChange(overtimeText.toDoubleOrNull() ?: overtimeMultiplier)
                        onSssChange(sssText.toDoubleOrNull() ?: sss)
                        onPhilHealthChange(philHealthText.toDoubleOrNull() ?: philHealth)
                        onPagIbigChange(pagIbigText.toDoubleOrNull() ?: pagIbig)
                        onOtherDeductionsChange(otherDeductionsText.toDoubleOrNull() ?: otherDeductions)
                        showDefaultsSavedDialog = true
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
            text = { Text("PayrollPro \u2014 Android Payroll Management System\nIT140P Machine Problem") }
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

    if (showRestUrlSavedDialog) {
        AlertDialog(
            onDismissRequest = { showRestUrlSavedDialog = false },
            confirmButton = {
                TextButton(onClick = { showRestUrlSavedDialog = false }) { Text("OK") }
            },
            title = { Text("REST URL Updated") },
            text = { Text("REST API base URL has been changed to:\n$restUrlText") }
        )
    }

    if (showDefaultsSavedDialog) {
        AlertDialog(
            onDismissRequest = { showDefaultsSavedDialog = false },
            confirmButton = {
                TextButton(onClick = { showDefaultsSavedDialog = false }) { Text("OK") }
            },
            title = { Text("Payroll Defaults Updated") },
            text = { Text("Your changes will be used the next time payroll is computed.") }
        )
    }
}