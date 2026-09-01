package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun NetworkTrafficChart(
    trafficData: List<Float>, // Dummy data points representing traffic/blocked ads over time
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Cyan
) {
    if (trafficData.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val maxData = trafficData.maxOrNull() ?: 1f
        val width = size.width
        val height = size.height
        val stepX = width / (trafficData.size - 1).coerceAtLeast(1)

        val path = Path()
        trafficData.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value / maxData) * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
