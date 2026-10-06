package com.drc.golftourbillion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class HoleMapPoint(val x: Float, val y: Float)

data class HoleMapData(
    val holeNumber: Int,
    val par: Int,
    val distanceMetres: Int,
    val fairwayRoute: List<HoleMapPoint> = emptyList()
)

@Composable
fun HoleMap(data: HoleMapData, yards: Boolean, modifier: Modifier = Modifier) {
    val gold = AppPalette.accent
    val panel = AppPalette.panel
    val white = AppPalette.text
    val muted = AppPalette.muted
    val mapTop = if (AppPalette.antiGlare) Color(0xFF30363B) else Color(0xFF143C2B)
    val mapMiddle = if (AppPalette.antiGlare) Color(0xFF20262B) else Color(0xFF092419)
    val mapBottom = if (AppPalette.antiGlare) Color(0xFF111518) else Color(0xFF061A12)
    val routeColor = if (AppPalette.antiGlare) Color(0xFFBFC5CA) else Color(0xFF82B879)
    val contourColor = if (AppPalette.antiGlare) Color(0xFFDDE1E4) else Color(0xFF8CA994)
    val hasRoute = data.fairwayRoute.size >= 2
    val shownDistance = if (yards) (data.distanceMetres * 1.09361).roundToInt() else data.distanceMetres
    val unit = if (yards) "yd" else "m"
    val distanceLabel = if (data.distanceMetres > 0) " • " + shownDistance + " " + unit else ""

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("HOLE " + data.holeNumber, color = gold, fontSize = 18.sp)
                    Text("COURSE SCHEMATIC", color = muted, fontSize = 10.sp)
                }
                Text("PAR " + data.par + distanceLabel, color = white, fontSize = 13.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (hasRoute) 210.dp else 112.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(mapTop, mapMiddle, mapBottom))),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    for (index in 0..4) {
                        val inset = 14.dp.toPx() + index * 15.dp.toPx()
                        drawOval(
                            color = contourColor.copy(alpha = if (AppPalette.antiGlare) 0.18f else 0.10f),
                            topLeft = androidx.compose.ui.geometry.Offset(inset, height * 0.18f + index * 6.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size((width - inset * 2).coerceAtLeast(0f), (height * 0.48f).coerceAtLeast(0f)),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    if (hasRoute) {
                        val points = data.fairwayRoute.map {
                            androidx.compose.ui.geometry.Offset(it.x.coerceIn(0f, 1f) * width, it.y.coerceIn(0f, 1f) * height)
                        }
                        val route = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            route,
                            routeColor,
                            style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            route,
                            Color.White.copy(alpha = 0.9f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 9.dp.toPx()))
                            )
                        )
                        drawCircle(gold, radius = 7.dp.toPx(), center = points.first())
                        drawCircle(Color.White, radius = 6.dp.toPx(), center = points.last())
                        drawCircle(
                            if (AppPalette.antiGlare) Color(0xFF20262B) else Color(0xFFB82020),
                            radius = 3.dp.toPx(),
                            center = points.last()
                        )
                    }
                }
                if (hasRoute) {
                    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("GREEN", color = white, fontSize = 10.sp)
                        Text("TEE", color = gold, fontSize = 10.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("VERIFIED HOLE MAP NOT AVAILABLE", color = gold, fontSize = 11.sp)
                        Text("No surveyed layout is saved for this hole.", color = white, fontSize = 11.sp)
                    }
                }
            }
            Text("Hole shape and target distances require verified course data.", color = muted, fontSize = 10.sp)
        }
    }
}
