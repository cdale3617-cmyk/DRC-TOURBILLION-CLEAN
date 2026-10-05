package com.drc.golftourbillion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LiveGold = Color(0xFFD4AF37)
private val LiveGreen = Color(0xFF071C14)
private val LivePanel = Color(0xFF0B2A1D)
private val LiveWhite = Color(0xFFF5F5F5)
private val LiveMuted = Color(0xFFB9C3BE)
private val LiveRed = Color(0xFF9E1B1B)

@Composable
fun LiveHole(
    courseName: String,
    holeNumber: Int,
    yards: Boolean,
    gpsStatus: String,
    windSummary: String,
    onHoleChange: (Int) -> Unit,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val course = GolfCourseCatalog.findByName(courseName)
    val hole = course?.hole(holeNumber)

    val par = hole?.par ?: 4
    val metres = hole?.metres ?: 0
    val shownDistance = if (yards) {
        (metres * 1.09361).toInt()
    } else {
        metres
    }
    val unit = if (yards) "yd" else "m"

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(LiveGreen)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE HOLE",
                        color = LiveGold,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = courseName,
                        color = LiveMuted,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "HOLE $holeNumber",
                    color = LiveWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiveInfoCard(
                    title = "GPS",
                    value = gpsStatus,
                    modifier = Modifier.weight(1f)
                )
                LiveInfoCard(
                    title = "WIND",
                    value = windSummary.ifBlank { "Waiting for GPS" },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (holeNumber > 1) onHoleChange(holeNumber - 1) },
                    enabled = holeNumber > 1,
                    border = BorderStroke(1.dp, LiveGold)
                ) {
                    Text("PREV", color = LiveGold, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PAR $par",
                        color = LiveGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (metres > 0) "$shownDistance $unit" else "DISTANCE —",
                        color = LiveWhite,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = { if (holeNumber < 18) onHoleChange(holeNumber + 1) },
                    enabled = holeNumber < 18,
                    border = BorderStroke(1.dp, LiveGold)
                ) {
                    Text("NEXT", color = LiveGold, fontWeight = FontWeight.Bold)
                }
            }

            HoleMap(
                data = HoleMapData(
                    holeNumber = holeNumber,
                    par = par,
                    distanceMetres = metres
                ),
                yards = yards,
                modifier = Modifier.fillMaxWidth()
            )

            LiveInfoCard(
                title = "CADDIE • ADVICE ONLY",
                value = "Choose a target and club after checking the live conditions.",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSelectTab("CADDIE") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiveRed,
                        contentColor = LiveGold
                    ),
                    border = BorderStroke(1.dp, LiveGold)
                ) {
                    Text("CADDIE", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSelectTab("SCORE") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LivePanel,
                        contentColor = LiveGold
                    ),
                    border = BorderStroke(1.dp, LiveGold)
                ) {
                    Text("SCORE", fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOOLS",
                color = LiveGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            LiveTab("DATA") { onSelectTab("DATA") }
            LiveTab("CADDIE") { onSelectTab("CADDIE") }
            LiveTab("PLAN") { onSelectTab("PLAN") }
            LiveTab("SCORE") { onSelectTab("SCORE") }
            LiveTab("GREEN") { onSelectTab("GREEN") }
            LiveTab("TRACER") { onSelectTab("TRACER") }
            LiveTab("GPS") { onSelectTab("GPS") }
            LiveTab("BAG") { onSelectTab("BAG") }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ADVICE ONLY",
                color = LiveMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LiveTab(
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            color = LiveGold,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LiveInfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = LivePanel),
        border = BorderStroke(1.dp, LiveGold.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = LiveGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = LiveWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
