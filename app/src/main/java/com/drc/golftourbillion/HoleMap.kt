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

data class HoleMapPoint(
    val x: Float,
    val y: Float
)

data class HoleMapData(
    val holeNumber: Int,
    val par: Int,
    val distanceMetres: Int,
    val fairwayRoute: List<HoleMapPoint> = emptyList()
)

@Composable
fun HoleMap(
    data: HoleMapData,
    yards: Boolean,
    modifier: Modifier = Modifier
) {
    val gold = Color(0xFFD4AF37)
    val panel = Color(0xFF0B2A1D)
    val white = Color(0xFFF5F5F5)
    val muted = Color(0xFFB9C3BE)
    val hasRoute = data.fairwayRoute.size >= 2
    val shownDistance = if (yards) {
        (data.distanceMetres * 1.09361).roundToInt()
    } else {
        data.distanceMetres
    }
    val unit = if (yards) "yd" else "m"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HOLE ${data.holeNumber}",
                        color = gold,
                        fontSize = 19.sp
                    )
                    Text(
                        text = "TOP-DOWN COURSE MAP",
                        color = muted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "PAR ${data.par}  •  $shownDistance $unit",
                    color = white,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF143C2B),
                                Color(0xFF092419),
                                Color(0xFF061A12)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Subtle topographic contour lines.
                    for (index in 0..5) {
                        val inset = 18.dp.toPx() + index * 22.dp.toPx()
                        drawOval(
                            color = Color(0xFF8CA994).copy(alpha = 0.10f),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                inset,
                                height * 0.18f + index * 9.dp.toPx()
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                (width - inset * 2).coerceAtLeast(0f),
                                (height * 0.48f).coerceAtLeast(0f)
                            ),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    if (hasRoute) {
                        val points = data.fairwayRoute.map {
                            androidx.compose.ui.geometry.Offset(
                                it.x.coerceIn(0f, 1f) * width,
                                it.y.coerceIn(0f, 1f) * height
                            )
                        }

                        val route = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { point ->
                                lineTo(point.x, point.y)
                            }
                        }

                        drawPath(
                            path = route,
                            color = Color(0xFF82B879),
                            style = Stroke(
                                width = 34.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        drawPath(
                            path = route,
                            color = Color.White.copy(alpha = 0.75f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(
                                        8.dp.toPx(),
                                        9.dp.toPx()
                                    )
                                )
                            )
                        )

                        val tee = points.first()
                        val green = points.last()

                        drawCircle(
                            color = gold,
                            radius = 8.dp.toPx(),
                            center = tee
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 7.dp.toPx(),
                            center = green
                        )
                        drawCircle(
                            color = Color(0xFFB82020),
                            radius = 3.dp.toPx(),
                            center = green
                        )
                    }
                }

                if (hasRoute) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GREEN", color = white, fontSize = 10.sp)
                        Text("TEE", color = gold, fontSize = 10.sp)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "COURSE MAP DATA REQUIRED",
                            color = gold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "No surveyed layout is available for this hole.",
                            color = white,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Text(
                text = "Map distances and shape require verified course data.",
                color = muted,
                fontSize = 11.sp
            )
        }
    }
}
