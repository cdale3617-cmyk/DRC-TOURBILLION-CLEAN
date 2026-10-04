package com.drc.golftourbillion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
private val SoftGold = Color(0xFFE6C866)
private val RacingGreen = Color(0xFF071C14)
private val PanelGreen = Color(0xFF0B2A1D)
private val RedAccent = Color(0xFF9E1B1B)

private enum class Screen {
    HOME,
    ROUND,
    BAG,
    LAB,
    MORE
}

@Composable
fun TourbillionApp() {

    var screen by remember { mutableStateOf(Screen.HOME) }
    var hole by remember { mutableIntStateOf(1) }
    var playerName by remember { mutableStateOf("Dale") }

    Scaffold(
        containerColor = RacingGreen,
        bottomBar = {

            Surface(
                color = Color(0xFF04110C),
                shadowElevation = 8.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 4.dp,
                            vertical = 4.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    TourNavItem(
                        text = "HOME",
                        selected = screen == Screen.HOME,
                        onClick = {
                            screen = Screen.HOME
                        }
                    )

                    TourNavItem(
                        text = "ROUND",
                        selected = screen == Screen.ROUND,
                        onClick = {
                            screen = Screen.ROUND
                        }
                    )

                    TourNavItem(
                        text = "BAG",
                        selected = screen == Screen.BAG,
                        onClick = {
                            screen = Screen.BAG
                        }
                    )

                    TourNavItem(
                        text = "LAB",
                        selected = screen == Screen.LAB,
                        onClick = {
                            screen = Screen.LAB
                        }
                    )

                    TourNavItem(
                        text = "MORE",
                        selected = screen == Screen.MORE,
                        onClick = {
                            screen
