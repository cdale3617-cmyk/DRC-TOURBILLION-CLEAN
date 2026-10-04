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
    HOME, ROUND, BAG, LAB, MORE
}

@Composable
fun TourbillionApp() {

    var screen by remember { mutableStateOf(Screen.HOME) }
    var hole by remember { mutableIntStateOf(1) }
    var playerName by remember { mutableStateOf("Dale") }

    Scaffold(
        containerColor = RacingGreen,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF04110C)
            ) {
                TourNavItem(
                    "HOME",
                    screen == Screen.HOME
                ) { screen = Screen.HOME }

                TourNavItem(
                    "ROUND",
                    screen == Screen.ROUND
                ) { screen = Screen.ROUND }

                TourNavItem(
                    "BAG",
                    screen == Screen.BAG
                ) { screen = Screen.BAG }

                TourNavItem(
                    "LAB",
                    screen == Screen.LAB
                ) { screen = Screen.LAB }

                TourNavItem(
                    "MORE",
                    screen == Screen.MORE
                ) { screen = Screen.MORE }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (screen) {

                Screen.HOME -> HomeScreen(
                    playerName = playerName,
                    onNameChange = { playerName = it },
                    startRound = {
                        screen = Screen.ROUND
                    }
                )

                Screen.ROUND -> LiveHoleScreen(
                    hole = hole,
                    previous = {
                        if (hole > 1) hole--
                    },
                    next = {
                        if (hole < 18) hole++
                    }
                )

                Screen.BAG -> BagScreen()

                Screen.LAB -> GolfLabScreen()

                Screen.MORE -> MoreScreen()
            }
        }
    }
}

@Composable
private fun TourNavItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Text(
                text.take(1),
                fontWeight = FontWeight.Black
            )
        },
        label = {
            Text(
                text,
                fontSize = 9.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Gold,
            selectedTextColor = Gold,
            unselectedIconColor = Color.LightGray,
            unselectedTextColor = Color.LightGray,
            indicatorColor = PanelGreen
        )
    )
}

@Composable
private fun TourHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Text(
            text = "DRC",
            color = SoftGold,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )

        Text(
            text = title,
            color = Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 13.sp
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = Gold.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun GoldCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 6.dp
            ),
        colors = CardDefaults.cardColors(
            containerColor = PanelGreen
        ),
        border = BorderStroke(
            1.dp,
            Gold.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun HomeScreen(
    playerName: String,
    onNameChange: (String) -> Unit,
    startRound: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            TourHeader(
                title = "GOLF TOURBILLION",
                subtitle = "Caddie Intelligence • Practice • Performance"
            )
        }

        item {
            GoldCard {
                Text(
                    text = "WELCOME",
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = playerName,
                    onValueChange = onNameChange,
                    label = {
                        Text("Player name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            GoldCard {

                Text(
                    text = "CADDIE ENGINE",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Course strategy, live hole control and scoring.",
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Button(
                    onClick = startRound,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedAccent
                    )
                ) {
                    Text(
                        text = "START CADDIE ENGINE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            HomeTile(
                title = "GPS",
                value = "Course positioning"
            )
        }

        item {
            HomeTile(
                title = "WEATHER",
                value = "Wind and conditions"
            )
        }

        item {
            HomeTile(
                title = "QUICK START",
                value = "Last course"
            )
        }

        item {
            HomeTile(
                title = "GOLF LAB",
                value = "Analytical Frameworks And Diagnostic Suite"
            )
        }
    }
}

@Composable
private fun HomeTile(
    title: String,
    value: String
) {
    GoldCard {

        Text(
            text = title,
            color = Gold,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = Color.White
        )
    }
}

@Composable
private fun LiveHoleScreen(
    hole: Int,
    previous: () -> Unit,
    next: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TourHeader(
            title = "LIVE HOLE",
            subtitle = "Hole $hole of 18"
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {

            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF103D27)
                ),
                border = BorderStroke(
                    1.dp,
                    Gold
                )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "HOLE",
                            color = SoftGold,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "$hole",
                            color = Gold,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "COURSE MAP / GPS",
                            color = Color.White.copy(
                                alpha = 0.7f
                            ),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp)
            ) {

                LiveBox(
                    "DISTANCES",
                    "Front —\nMiddle —\nBack —"
                )

                LiveBox(
                    "CADDIE",
                    "Next shot advice"
                )

                LiveBox(
                    "SCORE",
                    "Enter score"
                )

                LiveBox(
                    "WIND",
                    "L→R / R→L"
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            OutlinedButton(
                onClick = previous,
                modifier = Modifier.weight(1f),
                enabled = hole > 1
            ) {
                Text("PREV")
            }

            Button(
                onClick = next,
                modifier = Modifier.weight(1f),
                enabled = hole < 18
            ) {
                Text("NEXT")
            }
        }
    }
}

@Composable
private fun ColumnScope.LiveBox(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = PanelGreen
        ),
        border = BorderStroke(
            1.dp,
            Gold.copy(alpha = 0.55f)
        )
    ) {

        Column(
            modifier = Modifier.padding(9.dp)
        ) {

            Text(
                text = title,
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun BagScreen() {

    val clubs = listOf(
        Triple("Driver", "210 m", "10.5°"),
        Triple("3 Wood", "190 m", "15°"),
        Triple("5 Wood", "175 m", "18°"),
        Triple("4 Iron", "165 m", "22°"),
        Triple("5 Iron", "155 m", "25°"),
        Triple("6 Iron", "145 m", "28°"),
        Triple("7 Iron", "135 m", "32°"),
        Triple("8 Iron", "125 m", "36°"),
        Triple("9 Iron", "115 m", "41°"),
        Triple("PW", "100 m", "46°"),
        Triple("GW", "90 m", "50°"),
        Triple("SW", "75 m", "56°"),
        Triple("Putter", "—", "3°")
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TourHeader(
            title = "MY BAG",
            subtitle = "Club • Carry • Loft"
        )

        LazyColumn {

            items(clubs) { club ->

                GoldCard {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = club.first,
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = club.second,
                            color = SoftGold
                        )

                        Spacer(
                            modifier = Modifier.width(18.dp)
                        )

                        Text(
                            text = club.third,
                            color = Color.White.copy(
                                alpha = 0.75f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GolfLabScreen() {

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TourHeader(
            title =
                "Analytical Frameworks And Diagnostic Suite"
        )

        LazyColumn {

            items(tools) { tool ->

                GoldCard {

                    Text(
                        text = tool,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreScreen() {

    val tools = listOf(
        "Scorecard",
        "Strokes Gained",
        "Fusion",
        "Records",
        "Course Information",
        "Settings"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TourHeader(
            title = "MORE",
            subtitle = "Tourbillion tools and records"
        )

        tools.forEach { tool ->

            GoldCard {

                Text(
                    text = tool,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
