package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ChakraEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val outerRadius = w * 0.42f
        val innerHubRadius = w * 0.12f

        drawCircle(
            color = Color(0xFFF59E0B),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = innerHubRadius,
            center = center
        )
        drawCircle(
            color = Color(0xFFE65100),
            radius = innerHubRadius * 0.4f,
            center = center
        )

        val totalSpokes = 24
        for (i in 0 until totalSpokes) {
            val angle = Math.toRadians((i * (360.0 / totalSpokes))).toFloat()
            val startX = center.x + innerHubRadius * cos(angle.toDouble()).toFloat()
            val startY = center.y + innerHubRadius * sin(angle.toDouble()).toFloat()
            val endX = center.x + outerRadius * cos(angle.toDouble()).toFloat()
            val endY = center.y + outerRadius * sin(angle.toDouble()).toFloat()

            drawLine(
                color = if (i % 2 == 0) Color(0xFFF59E0B) else Color(0xFFFBBF24),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (i % 2 == 0) 2.5f else 1.5f
            )
        }
    }
}
