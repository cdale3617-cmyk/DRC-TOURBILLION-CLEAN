package com.drc.golftourbillion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** One persisted appearance switch controls every app surface. */
object AppPalette {
    var antiGlare by mutableStateOf(false)

    val background: Color get() = if (antiGlare) Color(0xFF080B0D) else Color(0xFF071C14)
    val panel: Color get() = if (antiGlare) Color(0xFF171B1F) else Color(0xFF0B2A1D)
    val raised: Color get() = if (antiGlare) Color(0xFF242A30) else Color(0xFF123626)
    val accent: Color get() = if (antiGlare) Color(0xFFD5D9DC) else Color(0xFFD4AF37)
    val accentSoft: Color get() = if (antiGlare) Color(0xFF9CA5AC) else Color(0xFFE6C866)
    val text: Color get() = if (antiGlare) Color(0xFFFFFFFF) else Color(0xFFF5F1E6)
    val muted: Color get() = if (antiGlare) Color(0xFFBEC4C9) else Color(0xFFB9C3BE)
    val action: Color get() = if (antiGlare) Color(0xFF343B42) else Color(0xFF9E1B1B)
    val border: Color get() = if (antiGlare) Color(0xFF737D85) else Color(0xFF8B7544)
}
