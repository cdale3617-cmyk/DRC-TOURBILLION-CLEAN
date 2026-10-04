package com.drc.golftourbillion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RacingGreen = Color(0xFF071C14)
private val DeepGreen = Color(0xFF0B2A1D)
private val MetallicGold = Color(0xFFD4AF37)
private val SoftGold = Color(0xFFE6C866)
private val Cream = Color(0xFFF5F1E6)

private val TourbillionDarkColors = darkColorScheme(
    primary = MetallicGold,
    onPrimary = RacingGreen,
    primaryContainer = DeepGreen,
    onPrimaryContainer = SoftGold,
    secondary = SoftGold,
    onSecondary = RacingGreen,
    background = RacingGreen,
    onBackground = Cream,
    surface = DeepGreen,
    onSurface = Cream,
    outline = MetallicGold
)

@Composable
fun DRCGolfTourbillionTheme(
    content: @Composable () ->
Unit
) {
    MaterialTheme(
        colorScheme = TourbillionDarkColors,
        content = content
    )
}
