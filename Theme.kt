package com.drc.golftourbillion.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Drc = darkColorScheme(primary=Color(0xFFD4AF37), secondary=Color(0xFFE6C866), background=Color(0xFF071C14), surface=Color(0xFF0B2A1D))
@Composable fun DRCGolfTourbillionTheme(content:@Composable()->Unit){ MaterialTheme(colorScheme=Drc, content=content) }
