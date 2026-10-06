package com.drc.golftourbillion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.drc.golftourbillion.AppPalette

@Composable
fun DRCGolfTourbillionTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = AppPalette.accent,
        onPrimary = AppPalette.background,
        primaryContainer = AppPalette.panel,
        onPrimaryContainer = AppPalette.text,
        secondary = AppPalette.accentSoft,
        onSecondary = AppPalette.background,
        background = AppPalette.background,
        onBackground = AppPalette.text,
        surface = AppPalette.panel,
        onSurface = AppPalette.text,
        surfaceVariant = AppPalette.raised,
        onSurfaceVariant = AppPalette.muted,
        outline = AppPalette.border
    )
    MaterialTheme(colorScheme = colors, content = content)
}
