package com.drc.golftourbillion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    val shownDistance = if (yards) (metres * 1.09361).toInt() else metres
    val unit = if (yards) "yd" else "m"

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(LiveGreen).padding(8.dp)
    ) {
        val wideLayout = maxWidth >= 700.dp
        if (wideLayout) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HoleContent(
                    courseName, holeNumber, par, metres, shownDistance, unit,
                    gpsStatus, windSummary, onHoleChange, onSelectTab,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    showHorizontalTabs = false
                )
                ToolRail(onSelectTab, Modifier.width(82.dp).fillMaxHeight())
            }
        } else {
            HoleContent(
                courseName, holeNumber, par, metres, shownDistance, unit,
                gpsStatus, windSummary, onHoleChange, onSelectTab,
                modifier = Modifier.fillMaxSize(),
                showHorizontalTabs = true
            )
        }
    }
}

@Composable
private fun HoleContent(
    courseName: String,
    holeNumber: Int,
    par: Int,
    metres: Int,
    shownDistance: Int,
    unit: String,
    gpsStatus: String,
    windSummary: String,
    onHoleChange: (Int) -> Unit,
    onSelectTab: (String) -> Unit,
    modifier: Modifier,
    showHorizontalTabs: Boolean
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LIVE HOLE", color = LiveGold, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text(courseName, color = LiveMuted, fontSize = 12.sp)
            }
            Text("HOLE $holeNumber", color = LiveWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (showHorizontalTabs) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("CADDIE", "SCORE", "BAG", "DATA", "PLAN", "GREEN", "TRACER", "GPS").forEach { tab ->
                    LiveTab(tab) { onSelectTab(tab) }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveInfoCard("GPS", gpsStatus, Modifier.weight(1f))
            LiveInfoCard("WIND", windSummary.ifBlank { "Waiting for GPS" }, Modifier.weight(1f))
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
            ) { Text("PREV", color = LiveGold, fontWeight = FontWeight.Bold) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PAR $par", color = LiveGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(if (metres > 0) "$shownDistance $unit" else "DISTANCE —", color = LiveWhite, fontSize = 14.sp)
            }
            OutlinedButton(
                onClick = { if (holeNumber < 18) onHoleChange(holeNumber + 1) },
                enabled = holeNumber < 18,
                border = BorderStroke(1.dp, LiveGold)
            ) { Text("NEXT", color = LiveGold, fontWeight = FontWeight.Bold) }
        }

        HoleMap(
            data = HoleMapData(holeNumber = holeNumber, par = par, distanceMetres = metres),
            yards = unit == "yd",
            modifier = Modifier.fillMaxWidth()
        )

        LiveInfoCard(
            title = "CADDIE • ADVICE ONLY",
            value = "Check the wind and your lie. Confirm the target and club before playing.",
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onSelectTab("CADDIE") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = LiveGold),
                border = BorderStroke(1.dp, LiveGold)
            ) { Text("CADDIE", fontWeight = FontWeight.Bold) }
            Button(
                onClick = { onSelectTab("SCORE") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = LivePanel, contentColor = LiveGold),
                border = BorderStroke(1.dp, LiveGold)
            ) { Text("SCORE", fontWeight = FontWeight.Bold) }
        }
        Text("Advice only • confirm conditions and distances on course", color = LiveMuted, fontSize = 11.sp)
    }
}

@Composable
private fun ToolRail(onSelectTab: (String) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TOOLS", color = LiveGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
        listOf("DATA", "CADDIE", "PLAN", "SCORE", "GREEN", "TRACER", "GPS", "BAG").forEach { tab ->
            LiveTab(tab) { onSelectTab(tab) }
        }
        Text("ADVICE ONLY", color = LiveMuted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LiveTab(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.width(76.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(label, color = LiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LiveInfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = LivePanel),
        border = BorderStroke(1.dp, LiveGold.copy(alpha = 0.8f))
    ) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = LiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = LiveWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
