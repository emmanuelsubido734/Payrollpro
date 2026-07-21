package com.payrollpro.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PayrollBlue,
    onPrimary = PayrollSurface,
    secondary = PayrollAccent,
    background = PayrollBackground,
    surface = PayrollSurface,
    error = PayrollError
)

private val DarkColors = darkColorScheme(
    primary = PayrollBlueDark,
    onPrimary = PayrollSurface,
    secondary = PayrollAccent,
    background = PayrollBackgroundDark,
    surface = PayrollSurfaceDark,
    error = PayrollError
)

@Composable
fun PayrollProTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
