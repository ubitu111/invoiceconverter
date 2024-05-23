package com.mamontov.invoice_converter.common.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { darkPalette }

@Composable
fun AppTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = if (isDarkTheme) darkPalette else lightPalette
    CompositionLocalProvider(
        LocalAppColors provides colors,
        content = content,
    )
}

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
