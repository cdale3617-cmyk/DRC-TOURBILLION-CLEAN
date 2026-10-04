package com.drc.golftourbillion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFD4AF37)
private val Green = Color(0xFF071C14)
private val Panel = Color(0xFF0B2A1D)
private val Red = Color(0xFF9E1B1B)

@Composable
fun TourbillionApp() {
    var screen by remember { mutableStateOf("HOME") }
    var hole by remember { mutableIntStateOf(1) }

    Scaffold(
        containerColor = Green,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding
