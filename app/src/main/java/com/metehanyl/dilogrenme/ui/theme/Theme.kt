package com.metehanyl.dilogrenme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF4F46E5)
private val IndigoDark = Color(0xFF4338CA)
private val Amber = Color(0xFFFACC15)
private val SurfaceLight = Color(0xFFF7F7FB)
private val SurfaceDarkCol = Color(0xFF15151F)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = Amber,
    onSecondary = Color(0xFF1E1E1E),
    background = SurfaceLight,
    surface = Color.White,
    error = Color(0xFFDC2626)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    secondary = Amber,
    onSecondary = Color(0xFF1E1E1E),
    background = SurfaceDarkCol,
    surface = Color(0xFF1F1F2B),
    error = Color(0xFFF87171)
)

@Composable
fun DilOgrenmeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

val GoodGreen = Color(0xFF22C55E)
val BadRed = Color(0xFFEF4444)
