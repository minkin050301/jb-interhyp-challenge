package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design inspired icons for navigation
 * Using Canvas-based drawing for cross-platform compatibility
 */
object NavIcons {
    @Composable
    fun HomeIcon(
        modifier: Modifier = Modifier,
        tint: Color = Color.Unspecified,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val canvasSize = this.size.width
            val scale = canvasSize / 24f
            val path = Path().apply {
                moveTo(10f * scale, 20f * scale)
                lineTo(10f * scale, 14f * scale)
                lineTo(14f * scale, 14f * scale)
                lineTo(14f * scale, 20f * scale)
                lineTo(19f * scale, 20f * scale)
                lineTo(19f * scale, 12f * scale)
                lineTo(22f * scale, 12f * scale)
                lineTo(12f * scale, 3f * scale)
                lineTo(2f * scale, 12f * scale)
                lineTo(5f * scale, 12f * scale)
                lineTo(5f * scale, 20f * scale)
                close()
            }
            drawPath(path, color = tint.takeIf { it != Color.Unspecified } ?: Color.Black)
        }
    }
    
    @Composable
    fun InsightsIcon(
        modifier: Modifier = Modifier,
        tint: Color = Color.Unspecified,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val canvasSize = this.size.width
            val scale = canvasSize / 24f
            val path = Path().apply {
                moveTo(3f * scale, 18f * scale)
                lineTo(21f * scale, 18f * scale)
                lineTo(21f * scale, 16f * scale)
                lineTo(3f * scale, 16f * scale)
                close()
                moveTo(3f * scale, 6f * scale)
                lineTo(3f * scale, 8f * scale)
                lineTo(21f * scale, 8f * scale)
                lineTo(21f * scale, 6f * scale)
                close()
                moveTo(3f * scale, 13f * scale)
                lineTo(12f * scale, 13f * scale)
                lineTo(12f * scale, 11f * scale)
                lineTo(3f * scale, 11f * scale)
                close()
            }
            drawPath(path, color = tint.takeIf { it != Color.Unspecified } ?: Color.Black)
        }
    }
    
    @Composable
    fun SettingsIcon(
        modifier: Modifier = Modifier,
        tint: Color = Color.Unspecified,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val canvasSize = this.size.width
            val centerX = canvasSize / 2f
            val centerY = canvasSize / 2f
            val radius = canvasSize * 0.3f
            
            // Draw gear using circles and lines
            drawCircle(
                color = tint.takeIf { it != Color.Unspecified } ?: Color.Black,
                radius = radius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = radius * 0.5f,
                center = Offset(centerX, centerY)
            )
            
            // Draw gear teeth
            val teethCount = 8
            val outerRadius = radius * 1.3f
            val innerRadius = radius * 0.9f
            for (i in 0 until teethCount) {
                val angle = (i * 360f / teethCount) * kotlin.math.PI / 180f
                val x1 = centerX + outerRadius * kotlin.math.cos(angle).toFloat()
                val y1 = centerY + outerRadius * kotlin.math.sin(angle).toFloat()
                val x2 = centerX + innerRadius * kotlin.math.cos(angle).toFloat()
                val y2 = centerY + innerRadius * kotlin.math.sin(angle).toFloat()
                
                drawLine(
                    color = tint.takeIf { it != Color.Unspecified } ?: Color.Black,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2f
                )
            }
        }
    }
}

// For compatibility with ImageVector-based API
data class IconVector(
    val name: String,
    val content: @Composable (Modifier, Color, Dp) -> Unit
)

val Home = IconVector("Home") { modifier, tint, size ->
    NavIcons.HomeIcon(modifier, tint, size)
}

val Insights = IconVector("Insights") { modifier, tint, size ->
    NavIcons.InsightsIcon(modifier, tint, size)
}

val Settings = IconVector("Settings") { modifier, tint, size ->
    NavIcons.SettingsIcon(modifier, tint, size)
}
