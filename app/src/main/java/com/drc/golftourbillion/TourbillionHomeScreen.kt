package com.drc.golftourbillion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeGold get() = AppPalette.accent
private val HomeGreen get() = AppPalette.background
private val HomePanel get() = AppPalette.panel
private val HomeRed get() = AppPalette.action
private val HomeWhite get() = AppPalette.text
private val HomeMuted get() = AppPalette.muted

@Composable
fun TourbillionHomeScreen(
    playerName: String,
    courseName: String,
    gpsStatus: String,
    weatherSummary: String,
    windSummary: String,
    yards: Boolean,
    onPlayerNameChange: (String) -> Unit,
    onCourseChange: () -> Unit,
    onUnitsToggle: () -> Unit,
    onStartRound: () -> Unit,
    onOpenBag: () -> Unit,
    onOpenLab: () -> Unit,
    onOpenScore: () -> Unit,
    onOpenHistory: () -> Unit,
    onToggleAntiGlare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlayerDialog by remember { mutableStateOf(false) }
    var editedPlayer by remember(playerName) {
        mutableStateOf(playerName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeGreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DRC",
                color = HomeGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggleAntiGlare) {
                    Text(if (AppPalette.antiGlare) "GLARE ON" else "GLARE OFF", color = HomeGold, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onUnitsToggle) {
                    Text(
                        text = if (yards) "YARDS" else "METRES",
                        color = HomeGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HeroPanel(
            playerName = playerName,
            onEditPlayer = { showPlayerDialog = true }
        )

        Button(
            onClick = onStartRound,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, HomeGold),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeRed,
                contentColor = HomeGold
            )
        ) {
            Text(
                text = "START CADDIE ENGINE",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
        }

        SectionHeading("LIVE COURSE CONDITIONS")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            StatusPanel(
                title = "GPS",
                detail = gpsStatus,
                modifier = Modifier.weight(1f)
            )
            StatusPanel(
                title = "WEATHER",
                detail = weatherSummary,
                modifier = Modifier.weight(1f)
            )
        }

        StatusPanel(
            title = "WIND",
            detail = windSummary,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = onCourseChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, HomeGold)
        ) {
            Text(
                text = "COURSE  •  $courseName",
                color = HomeGold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        SectionHeading("YOUR GOLF TOOLS")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            HomeTile(
                title = "MY BAG",
                detail = "Clubs and carries",
                modifier = Modifier.weight(1f),
                onClick = onOpenBag
            )
            HomeTile(
                title = "GOLF LAB",
                detail = "Practice and analysis",
                modifier = Modifier.weight(1f),
                onClick = onOpenLab
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            HomeTile(
                title = "SCORECARD",
                detail = "Current round",
                modifier = Modifier.weight(1f),
                onClick = onOpenScore
            )
            HomeTile(
                title = "ROUND HISTORY",
                detail = "Saved rounds",
                modifier = Modifier.weight(1f),
                onClick = onOpenHistory
            )
        }

        Text(
            text = "Practice like you’re playing.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            color = HomeMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }

    if (showPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            containerColor = HomePanel,
            title = {
                Text("Player name", color = HomeGold)
            },
            text = {
                OutlinedTextField(
                    value = editedPlayer,
                    onValueChange = { editedPlayer = it },
                    singleLine = true,
                    label = {
                        Text("Name", color = HomeMuted)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = editedPlayer.trim()
                        if (name.isNotEmpty()) {
                            onPlayerNameChange(name)
                        }
                        showPlayerDialog = false
                    }
                ) {
                    Text("SAVE", color = HomeGold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPlayerDialog = false }
                ) {
                    Text("CANCEL", color = HomeWhite)
                }
            }
        )
    }
}

@Composable
private fun HeroPanel(
    playerName: String,
    onEditPlayer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (AppPalette.antiGlare) Color(0xFF252A2E) else Color(0xFF163B2C),
                            if (AppPalette.antiGlare) Color(0xFF151A1E) else Color(0xFF0B2A1D),
                            if (AppPalette.antiGlare) Color(0xFF090C0E) else Color(0xFF06150F)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
        ) {
            val width = size.width
            val height = size.height

            for (index in 0..5) {
                val inset = 15.dp.toPx() + index * 17.dp.toPx()
                drawOval(
                    color = (if (AppPalette.antiGlare) Color(0xFFDBE0E3) else Color(0xFFB1C3A3)).copy(alpha = if (AppPalette.antiGlare) 0.20f else 0.10f),
                    topLeft = Offset(
                        inset,
                        height * 0.35f + index * 6.dp.toPx()
                    ),
                    size = Size(
                        (width - inset * 2).coerceAtLeast(0f),
                        height * 0.44f
                    ),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            val fairway = Path().apply {
                moveTo(width * 0.48f, height * 1.02f)
                cubicTo(
                    width * 0.28f,
                    height * 0.80f,
                    width * 0.68f,
                    height * 0.63f,
                    width * 0.49f,
                    height * 0.43f
                )
                cubicTo(
                    width * 0.39f,
                    height * 0.31f,
                    width * 0.53f,
                    height * 0.24f,
                    width * 0.56f,
                    height * 0.12f
                )
            }

            drawPath(
                path = fairway,
                brush = Brush.verticalGradient(
                    listOf(
                        if (AppPalette.antiGlare) Color(0xFFBCC2C7) else Color(0xFF5D8750),
                        if (AppPalette.antiGlare) Color(0xFF8C959C) else Color(0xFF367044),
                        if (AppPalette.antiGlare) Color(0xFF535D64) else Color(0xFF245C38)
                    )
                ),
                style = Stroke(
                    width = 70.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            drawOval(
                color = if (AppPalette.antiGlare) Color(0xFFD5D9DC) else Color(0xFF79A95D),
                topLeft = Offset(width * 0.40f, height * 0.05f),
                size = Size(width * 0.32f, height * 0.10f)
            )

            drawCircle(
                color = HomeGold,
                radius = 5.dp.toPx(),
                center = Offset(width * 0.48f, height * 0.91f)
            )

            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(width * 0.56f, height * 0.10f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
        ) {
            Text(
                text = "GOLF",
                color = HomeWhite,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "TOURBILLION",
                color = HomeGold,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "WELCOME, ${playerName.uppercase()}",
                color = HomeWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = onEditPlayer,
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "EDIT PLAYER",
                    color = HomeGold,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = "18 HOLES  •  ADVICE ONLY",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            color = HomeWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        color = HomeGold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
}

@Composable
private fun StatusPanel(
    title: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HomePanel),
        border = BorderStroke(1.dp, HomeGold.copy(alpha = 0.75f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                color = HomeGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                color = HomeWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HomeTile(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HomePanel),
        border = BorderStroke(1.dp, HomeGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = HomeGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                color = HomeWhite,
                fontSize = 11.sp
            )
        }
    }
}
