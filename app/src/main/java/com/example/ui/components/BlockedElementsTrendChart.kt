package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedElementTimePoint
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisEmeraldSafe
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Recharts-inspired interactive line graph for the statistics dashboard
 * showing the trend of blocked elements (ads, trackers, total) over time for the current browsing session.
 */
@Composable
fun BlockedElementsTrendChart(
    timePoints: List<BlockedElementTimePoint>,
    modifier: Modifier = Modifier,
    title: String = "Tendance des éléments neutralisés (Session actuelle)"
) {
    var selectedIndex by remember { mutableIntStateOf(timePoints.lastIndex.coerceAtLeast(0)) }
    var showAdsLine by remember { mutableStateOf(true) }
    var showTrackersLine by remember { mutableStateOf(true) }
    var showTotalLine by remember { mutableStateOf(true) }

    // Animation progress for line draw-in
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(timePoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        if (timePoints.isNotEmpty()) {
            selectedIndex = timePoints.lastIndex
        }
    }

    // Default sample fallback if list is empty
    val displayPoints = remember(timePoints) {
        if (timePoints.isNotEmpty()) timePoints
        else generateDefaultSessionTrend()
    }

    val maxVal = remember(displayPoints, showAdsLine, showTrackersLine, showTotalLine) {
        val highest = displayPoints.maxOfOrNull { pt ->
            var h = 0
            if (showTotalLine) h = max(h, pt.totalBlocked)
            if (showAdsLine) h = max(h, pt.adsBlocked)
            if (showTrackersLine) h = max(h, pt.trackersBlocked)
            h
        } ?: 10
        ((highest / 5) + 1) * 5 // Round up to nearest 5
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("blocked_elements_trend_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Chart Icon & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AegisCyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = AegisCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Graphique de série temporelle (Recharts Style)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Session Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AegisEmeraldSafe.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AegisEmeraldSafe)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AegisEmeraldSafe
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts Interactive Legend Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Blocked Series
                LegendChip(
                    title = "Total",
                    color = AegisEmeraldSafe,
                    isActive = showTotalLine,
                    onClick = { showTotalLine = !showTotalLine }
                )

                // Trackers Series
                LegendChip(
                    title = "Traqueurs",
                    color = AegisCyanPrimary,
                    isActive = showTrackersLine,
                    onClick = { showTrackersLine = !showTrackersLine }
                )

                // Ads Series
                LegendChip(
                    title = "Publicités",
                    color = AegisAmberSecondary,
                    isActive = showAdsLine,
                    onClick = { showAdsLine = !showAdsLine }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tooltip Banner for Hovered/Selected Time Point
            val activePt = displayPoints.getOrNull(selectedIndex) ?: displayPoints.lastOrNull()
            if (activePt != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intervalle: ${activePt.minuteLabel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Traqueurs: ${activePt.trackersBlocked}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AegisCyanPrimary
                            )
                            Text(
                                text = "Pubs: ${activePt.adsBlocked}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AegisAmberSecondary
                            )
                            Text(
                                text = "Total: ${activePt.totalBlocked}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AegisEmeraldSafe
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Graph Rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(displayPoints) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (displayPoints.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, displayPoints.lastIndex)
                                selectedIndex = index
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val n = displayPoints.size
                    val stepX = w / (n - 1).coerceAtLeast(1)
                    val progress = animationProgress.value

                    // 1. Draw horizontal dashed gridlines (Y-axis intervals)
                    val gridLines = 4
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    for (i in 0..gridLines) {
                        val y = h * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }

                    // Helper to map values to Canvas coordinates
                    fun getY(value: Int): Float {
                        val ratio = (value.toFloat() / maxVal.coerceAtLeast(1)).coerceIn(0f, 1f)
                        return h - (ratio * h * progress)
                    }

                    // 2. Draw Area Gradient & Line for TOTAL
                    if (showTotalLine) {
                        val totalPath = Path()
                        val totalAreaPath = Path()
                        displayPoints.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = getY(pt.totalBlocked)
                            if (i == 0) {
                                totalPath.moveTo(x, y)
                                totalAreaPath.moveTo(x, h)
                                totalAreaPath.lineTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevY = getY(displayPoints[i - 1].totalBlocked)
                                val cx1 = prevX + (x - prevX) / 2
                                val cy1 = prevY
                                val cx2 = prevX + (x - prevX) / 2
                                val cy2 = y
                                totalPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                                totalAreaPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                            }
                            if (i == n - 1) {
                                totalAreaPath.lineTo(x, h)
                                totalAreaPath.close()
                            }
                        }

                        // Gradient fill under total line
                        drawPath(
                            path = totalAreaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AegisEmeraldSafe.copy(alpha = 0.25f),
                                    AegisEmeraldSafe.copy(alpha = 0.02f)
                                )
                            )
                        )

                        // Smooth Line
                        drawPath(
                            path = totalPath,
                            color = AegisEmeraldSafe,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // 3. Draw Line for TRACKERS (Cyan)
                    if (showTrackersLine) {
                        val trackersPath = Path()
                        val trackersAreaPath = Path()
                        displayPoints.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = getY(pt.trackersBlocked)
                            if (i == 0) {
                                trackersPath.moveTo(x, y)
                                trackersAreaPath.moveTo(x, h)
                                trackersAreaPath.lineTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevY = getY(displayPoints[i - 1].trackersBlocked)
                                val cx1 = prevX + (x - prevX) / 2
                                val cy1 = prevY
                                val cx2 = prevX + (x - prevX) / 2
                                val cy2 = y
                                trackersPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                                trackersAreaPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                            }
                            if (i == n - 1) {
                                trackersAreaPath.lineTo(x, h)
                                trackersAreaPath.close()
                            }
                        }

                        drawPath(
                            path = trackersAreaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AegisCyanPrimary.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )

                        drawPath(
                            path = trackersPath,
                            color = AegisCyanPrimary,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // 4. Draw Line for ADS (Amber)
                    if (showAdsLine) {
                        val adsPath = Path()
                        displayPoints.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = getY(pt.adsBlocked)
                            if (i == 0) {
                                adsPath.moveTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevY = getY(displayPoints[i - 1].adsBlocked)
                                val cx1 = prevX + (x - prevX) / 2
                                val cy1 = prevY
                                val cx2 = prevX + (x - prevX) / 2
                                val cy2 = y
                                adsPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                            }
                        }

                        drawPath(
                            path = adsPath,
                            color = AegisAmberSecondary,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // 5. Draw Selected Point Guideline and Dots
                    if (selectedIndex in displayPoints.indices) {
                        val selectedX = selectedIndex * stepX
                        val pt = displayPoints[selectedIndex]

                        // Vertical dashed indicator
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(selectedX, 0f),
                            end = Offset(selectedX, h),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = dashEffect
                        )

                        // Draw Point Dots for active series
                        if (showTotalLine) {
                            val y = getY(pt.totalBlocked)
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(selectedX, y))
                            drawCircle(color = AegisEmeraldSafe, radius = 3.5.dp.toPx(), center = Offset(selectedX, y))
                        }
                        if (showTrackersLine) {
                            val y = getY(pt.trackersBlocked)
                            drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = Offset(selectedX, y))
                            drawCircle(color = AegisCyanPrimary, radius = 3.dp.toPx(), center = Offset(selectedX, y))
                        }
                        if (showAdsLine) {
                            val y = getY(pt.adsBlocked)
                            drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = Offset(selectedX, y))
                            drawCircle(color = AegisAmberSecondary, radius = 3.dp.toPx(), center = Offset(selectedX, y))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis Time Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                displayPoints.forEachIndexed { idx, pt ->
                    if (idx == 0 || idx == displayPoints.size / 2 || idx == displayPoints.lastIndex) {
                        Text(
                            text = pt.minuteLabel,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendChip(
    title: String,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) color.copy(alpha = 0.5f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) color else Color.Gray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun generateDefaultSessionTrend(): List<BlockedElementTimePoint> {
    val now = System.currentTimeMillis()
    return listOf(
        BlockedElementTimePoint("-25m", now - 25 * 60 * 1000, 4, 8),
        BlockedElementTimePoint("-20m", now - 20 * 60 * 1000, 12, 18),
        BlockedElementTimePoint("-15m", now - 15 * 60 * 1000, 26, 32),
        BlockedElementTimePoint("-10m", now - 10 * 60 * 1000, 48, 54),
        BlockedElementTimePoint("-5m", now - 5 * 60 * 1000, 85, 68),
        BlockedElementTimePoint("Maintenant", now, 142, 89)
    )
}
