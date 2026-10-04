package com.drc.golftourbillion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFD4AF37)
private val Green = Color(0xFF071C14)
private val Panel = Color(0xFF0B2A1D)
private val Red = Color(0xFF9E1B1B)
private val White = Color(0xFFF5F5F5)

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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                BottomButton(
                    text = "HOME",
                    selected = screen == "HOME"
                ) {
                    screen = "HOME"
                }

                BottomButton(
                    text = "CADDIE",
                    selected = screen == "CADDIE"
                ) {
                    screen = "CADDIE"
                }

                BottomButton(
                    text = "LAB",
                    selected = screen == "LAB"
                ) {
                    screen = "LAB"
                }

                BottomButton(
                    text = "SCORE",
                    selected = screen == "SCORE"
                ) {
                    screen = "SCORE"
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (screen) {

                "HOME" -> HomeScreen(
                    onStart = {
                        hole = 1
                        screen = "CADDIE"
                    }
                )

                "CADDIE" -> CaddieScreen(
                    hole = hole,
                    onPrevious = {
                        if (hole > 1) {
                            hole--
                        }
                    },
                    onNext = {
                        if (hole < 18) {
                            hole++
                        }
                    }
                )

                "LAB" -> LabScreen()

                "SCORE" -> ScoreScreen(
                    hole = hole
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onStart: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DRC",
            color = Gold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "GOLF TOURBILLION",
            color = Gold,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Precision Golf Caddie",
            color = White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        GoldPanel(
            title = "LIVE CONDITIONS",
            body = "GPS • Weather • Wind • Course"
        )

        Spacer(modifier = Modifier.height(14.dp))

        GoldPanel(
            title = "MY BAG",
            body = "Club distances • Loft • Carry"
        )

        Spacer(modifier = Modifier.height(14.dp))

        GoldPanel(
            title = "GOLF LAB",
            body = "Analytical Frameworks And Diagnostic Suite"
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Red,
                contentColor = Gold
            ),
            border = BorderStroke(1.dp, Gold),
            shape = RoundedCornerShape(12.dp)
        ) {

            Text(
                text = "START CADDIE ENGINE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CaddieScreen(
    hole: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            text = "LIVE CADDIE",
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Panel
            ),
            border = BorderStroke(1.dp, Gold),
            shape = RoundedCornerShape(14.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "HOLE $hole",
                    color = Gold,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (hole == 1) "PAR 5" else "COURSE HOLE",
                    color = White,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "COURSE MAP\n\nGPS COURSE VIEW",
                        color = Gold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    color = Gold.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Distances   •   Wind   •   Caddie Advice",
                    color = White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OutlinedButton(
                onClick = onPrevious,
                enabled = hole > 1,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, Gold),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Gold
                )
            ) {
                Text("PREV")
            }

            OutlinedButton(
                onClick = onNext,
                enabled = hole < 18,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, Gold),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Gold
                )
            ) {
                Text("NEXT")
            }
        }
    }
}

@Composable
private fun LabScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Analytical Frameworks And Diagnostic Suite",
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        val tools = listOf(
            "Swing Monitor",
            "Shot Tracer",
            "Shot Pattern / Dispersion",
            "Putting Practice",
            "Short Game",
            "Wedge Distances",
            "Greenside Chipping",
            "Greenslope",
            "Score Comparison",
            "Biometrics",
            "Round Overview",
            "Pre-Round",
            "Club Equipment",
            "Round History",
            "How To"
        )

        tools.forEach { tool ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                ),
                border = BorderStroke(1.dp, Gold),
                shape = RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = tool,
                    modifier = Modifier.padding(16.dp),
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ScoreScreen(
    hole: Int
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "SCORECARD",
            color = Gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        GoldPanel(
            title = "CURRENT HOLE",
            body = "Hole $hole"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GoldPanel(
            title = "ROUND",
            body = "18 Hole Scorecard"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GoldPanel(
            title = "STATUS",
            body = "Round in progress"
        )
    }
}

@Composable
private fun GoldPanel(
    title: String,
    body: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Panel
        ),
        border = BorderStroke(1.dp, Gold),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = body,
                color = White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun BottomButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected) Gold else White
        )
    ) {

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}
