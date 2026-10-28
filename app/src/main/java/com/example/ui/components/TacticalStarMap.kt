package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GalaxyGenerator
import com.example.model.GameState
import com.example.model.StarSystem
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalStarMap(
    gameState: GameState,
    selectedSystem: StarSystem?,
    onSystemSelected: (StarSystem) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.6f, 2.5f)
        offset += offsetChange
    }

    // Radar scan rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .testTag("tactical_star_map")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState)
                .pointerInput(gameState.ticks) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        val minX = 0f
                        val maxX = 46f
                        val minY = 0f
                        val maxY = 26f

                        // Find closest star system to tap
                        var closest: StarSystem? = null
                        var closestDist = Float.MAX_VALUE

                        for ((_, sys) in gameState.systems) {
                            val normX = (sys.posX - minX) / (maxX - minX)
                            val normY = (sys.posY - minY) / (maxY - minY)
                            val screenX = (normX * w * 0.85f + w * 0.08f) * scale + offset.x
                            val screenY = (normY * h * 0.85f + h * 0.08f) * scale + offset.y

                            val dx = tapOffset.x - screenX
                            val dy = tapOffset.y - screenY
                            val distSq = dx * dx + dy * dy

                            if (distSq < (42f * scale) * (42f * scale) && distSq < closestDist) {
                                closestDist = distSq
                                closest = sys
                            }
                        }

                        if (closest != null) {
                            onSystemSelected(closest)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f + offset.x, h / 2f + offset.y)

            // 1. Draw Tactical Radar Circles
            val maxRadius = (minOf(w, h) * 0.85f) * scale
            for (step in 1..4) {
                drawCircle(
                    color = RadarGridColor,
                    radius = (maxRadius * (step / 4f)),
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }

            // Radar Crosshairs
            drawLine(
                color = RadarGridColor,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = RadarGridColor,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )

            // Radar Rotating Sweep Beam
            val rad = Math.toRadians(sweepAngle.toDouble())
            val sweepEnd = Offset(
                (center.x + maxRadius * cos(rad)).toFloat(),
                (center.y + maxRadius * sin(rad)).toFloat()
            )
            drawLine(
                color = TacticalCyan.copy(alpha = 0.25f),
                start = center,
                end = sweepEnd,
                strokeWidth = 1.5f
            )

            val minX = 0f
            val maxX = 46f
            val minY = 0f
            val maxY = 26f

            fun coordsToScreen(x: Float, y: Float): Offset {
                val normX = (x - minX) / (maxX - minX)
                val normY = (y - minY) / (maxY - minY)
                return Offset(
                    (normX * w * 0.85f + w * 0.08f) * scale + offset.x,
                    (normY * h * 0.85f + h * 0.08f) * scale + offset.y
                )
            }

            // 2. Draw Wormholes (Pulsing Violet Dashed Vectors)
            val wormholeVisited = mutableSetOf<Pair<String, String>>()
            for ((srcId, dstId) in GalaxyGenerator.WORMHOLES) {
                val pair = if (srcId < dstId) srcId to dstId else dstId to srcId
                if (wormholeVisited.add(pair)) {
                    val s1 = gameState.systems[srcId]
                    val s2 = gameState.systems[dstId]
                    if (s1 != null && s2 != null) {
                        val p1 = coordsToScreen(s1.posX, s1.posY)
                        val p2 = coordsToScreen(s2.posX, s2.posY)
                        drawLine(
                            color = NebulaPurple.copy(alpha = 0.6f),
                            start = p1,
                            end = p2,
                            strokeWidth = 2.2f * scale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    }
                }
            }

            // 3. Draw Hyperlanes (Cyan/Blue Connection Vectors)
            val drawnHyperlanes = mutableSetOf<Pair<String, String>>()
            for ((sysId, sys) in gameState.systems) {
                val p1 = coordsToScreen(sys.posX, sys.posY)
                for (nId in sys.neighbors) {
                    val pair = if (sysId < nId) sysId to nId else nId to sysId
                    if (drawnHyperlanes.add(pair)) {
                        val neighbor = gameState.systems[nId]
                        if (neighbor != null) {
                            val p2 = coordsToScreen(neighbor.posX, neighbor.posY)
                            val isAllyOrSameOwner = sys.owner != null && sys.owner == neighbor.owner
                            val laneColor = if (isAllyOrSameOwner) {
                                when (sys.owner) {
                                    "solarian" -> FactionABlue.copy(alpha = 0.55f)
                                    "zergon" -> FactionBRed.copy(alpha = 0.55f)
                                    "federation" -> FactionCGreen.copy(alpha = 0.55f)
                                    else -> TacticalCyan.copy(alpha = 0.35f)
                                }
                            } else {
                                TacticalCyanMuted.copy(alpha = 0.25f)
                            }
                            drawLine(
                                color = laneColor,
                                start = p1,
                                end = p2,
                                strokeWidth = 1.4f * scale
                            )
                        }
                    }
                }
            }

            // 4. Draw System Nodes
            for ((_, sys) in gameState.systems) {
                val pos = coordsToScreen(sys.posX, sys.posY)
                val isSelected = selectedSystem?.id == sys.id
                val isCapital = sys.capital
                val nodeColor = when (sys.owner) {
                    "solarian" -> FactionABlue
                    "zergon" -> FactionBRed
                    "federation" -> FactionCGreen
                    "pirates" -> LaserRed
                    else -> FactionNeutral
                }

                // Selection glow ring
                if (isSelected) {
                    drawCircle(
                        color = StellarGold.copy(alpha = 0.35f),
                        radius = 26f * scale,
                        center = pos
                    )
                    drawCircle(
                        color = StellarGold,
                        radius = 24f * scale,
                        center = pos,
                        style = Stroke(width = 2.5f)
                    )
                }

                // Outer orbital boundary ring
                drawCircle(
                    color = nodeColor.copy(alpha = 0.85f),
                    radius = if (isCapital) 17f * scale else 13f * scale,
                    center = pos,
                    style = Stroke(width = if (isCapital) 3f else 2f)
                )

                // Inner core dot
                drawCircle(
                    color = if (isCapital) StellarGold else nodeColor,
                    radius = if (isCapital) 8f * scale else 6f * scale,
                    center = pos
                )

                // Player Ship indicator (if at this location in RPG mode)
                if (gameState.playerShip.location == sys.id) {
                    drawCircle(
                        color = TacticalCyan,
                        radius = 21f * scale,
                        center = pos,
                        style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                    )
                }

                // System Text Label
                val textLayout = textMeasurer.measure(
                    text = if (isCapital) "★ ${sys.id}" else sys.id,
                    style = TextStyle(
                        color = if (isSelected) StellarGold else TextPrimary,
                        fontSize = (11f * scale).coerceIn(9f, 15f).sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isCapital || isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(pos.x - textLayout.size.width / 2f, pos.y + 16f * scale)
                )
            }

            // 5. Draw Stationed Fleets as Vector Arrowheads (▲)
            for (fleet in gameState.fleets) {
                val sys = gameState.systems[fleet.location] ?: continue
                val basePos = coordsToScreen(sys.posX, sys.posY)

                // Offset multiple fleets slightly
                val fleetColor = when (fleet.owner) {
                    "solarian" -> FactionABlue
                    "zergon" -> FactionBRed
                    "federation" -> FactionCGreen
                    "pirates" -> LaserRed
                    else -> StellarGold
                }

                val arrowSize = 9f * scale
                val tipY = basePos.y - (14f * scale)
                val path = Path().apply {
                    moveTo(basePos.x, tipY - arrowSize)
                    lineTo(basePos.x - arrowSize * 0.7f, tipY + arrowSize * 0.6f)
                    lineTo(basePos.x, tipY + arrowSize * 0.2f)
                    lineTo(basePos.x + arrowSize * 0.7f, tipY + arrowSize * 0.6f)
                    close()
                }
                drawPath(path = path, color = fleetColor)
            }
        }

        // Overlay Navigation Controls (Zoom In, Zoom Out, Reset Center)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { scale = (scale * 1.25f).coerceAtMost(2.5f) },
                containerColor = SpaceCardBg,
                contentColor = TacticalCyan,
                shape = CircleShape
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
            }
            SmallFloatingActionButton(
                onClick = { scale = (scale / 1.25f).coerceAtLeast(0.6f) },
                containerColor = SpaceCardBg,
                contentColor = TacticalCyan,
                shape = CircleShape
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
            }
            SmallFloatingActionButton(
                onClick = {
                    scale = 1.0f
                    offset = Offset.Zero
                },
                containerColor = SpaceCardBg,
                contentColor = StellarGold,
                shape = CircleShape
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View")
            }
        }
    }
}
