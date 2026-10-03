package com.drc.golftourbillion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RacingGreen = Color(0xFF071C14)
private val DeepGreen = Color(0xFF0B2A1D)
private val MetallicGold = Color(0xFFD4AF37)
private val SoftGold = Color(0xFFF1D77A)
private val TourbillionBlack = Color(0xFF050706)
private val OffWhite = Color(0xFFF5F2E8)
private val TourbillionRed = Color(0xFF9E1B1B)

private val TourbillionColors = darkColorScheme(
    primary = MetallicGold,
    onPrimary = TourbillionBlack,
    primaryContainer = DeepGreen,
    onPrimaryContainer = SoftGold,

    secondary = SoftGold,
    onSecondary = TourbillionBlack,

    tertiary = TourbillionRed,
    onTertiary = OffWhite,

    background = RacingGreen,
    onBackground = OffWhite,

    surface = TourbillionBlack,
    onSurface = OffWhite,

    surfaceVariant = DeepGreen,
    onSurfaceVariant = SoftGold,

    outline = MetallicGold
)

@Composable
fun DRCGolfTourbillionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TourbillionColors,
