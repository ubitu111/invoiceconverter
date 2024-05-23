package com.mamontov.invoice_converter.common.ui.theme

import androidx.compose.ui.graphics.Color

data class AppColors(
    val accent: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
)

val lightPalette = AppColors(
    accent = Color(0xFFFFF59D),
    surface = Color(0xFFFF8DC2A6),
    onSurface = Color(0xFF133050),
    background = Color(0xFFD7FFEA),
    onBackground = Color(0xFF001329),
)

val darkPalette = AppColors(
    accent = Color(0xFFAF9363),
    surface = Color(0xFF0D1E31),
    onSurface = Color(0xFF99A6B5),
    background = Color(0xFF060D16),
    onBackground = Color(0xFFF6F6F6),
)
