package com.payrollpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.payrollpro.app.navigation.PayrollNavGraph
import com.payrollpro.app.ui.theme.PayrollProTheme
import com.payrollpro.app.viewmodel.PayrollViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PayrollViewModel = viewModel()
            PayrollProTheme(darkTheme = viewModel.isDarkTheme.value) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    PayrollNavGraph(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}