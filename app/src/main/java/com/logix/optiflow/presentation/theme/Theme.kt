package com.logix.optiflow.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val OptiFlowColorScheme =
    lightColorScheme(
        primary = Electric,
        onPrimary = White,
        primaryContainer = SkyTint,
        secondary = NavyBlue,
        background = ScreenBg,
        onBackground = DeepNavy,
        surface = White,
        onSurface = DeepNavy,
        error = ErrorRed,
    )

private val base = TextStyle(fontFamily = Jakarta)

private val OptiFlowTypography =
    Typography(
        bodyLarge = base,
        bodyMedium = base,
        bodySmall = base,
        labelLarge = base,
        labelMedium = base,
        labelSmall = base,
        titleLarge = base,
        titleMedium = base,
        titleSmall = base,
    )

@Composable
fun OptiFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OptiFlowColorScheme,
        typography = OptiFlowTypography,
        content = content,
    )
}
