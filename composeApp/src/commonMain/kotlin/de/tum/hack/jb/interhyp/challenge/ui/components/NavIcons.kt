package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
            val scale = canvasSize / 24f
            val centerX = canvasSize / 2f
            val centerY = canvasSize / 2f
            val color = tint.takeIf { it != Color.Unspecified } ?: Color.Black
            
            // Standard gear/cog icon - Apple/Material Design style
            val outerRadius = 9f * scale
            val innerRadius = 5.5f * scale
            val toothLength = 2.5f * scale
            val toothWidth = 1.5f * scale
            val numTeeth = 8
            
            val path = Path().apply {
                // Draw gear shape with teeth
                for (i in 0 until numTeeth) {
                    val angle1 = (i * 360f / numTeeth - 360f / (numTeeth * 2)) * kotlin.math.PI / 180f
                    val angle2 = (i * 360f / numTeeth) * kotlin.math.PI / 180f
                    val angle3 = (i * 360f / numTeeth + 360f / (numTeeth * 2)) * kotlin.math.PI / 180f
                    
                    // Outer point of tooth
                    val outerX1 = centerX + (outerRadius + toothLength) * kotlin.math.cos(angle1).toFloat()
                    val outerY1 = centerY + (outerRadius + toothLength) * kotlin.math.sin(angle1).toFloat()
                    
                    // Inner point before tooth
                    val innerX1 = centerX + outerRadius * kotlin.math.cos(angle1).toFloat()
                    val innerY1 = centerY + outerRadius * kotlin.math.sin(angle1).toFloat()
                    
                    // Outer point at tooth tip
                    val outerX2 = centerX + (outerRadius + toothLength) * kotlin.math.cos(angle2).toFloat()
                    val outerY2 = centerY + (outerRadius + toothLength) * kotlin.math.sin(angle2).toFloat()
                    
                    // Inner point after tooth
                    val innerX2 = centerX + outerRadius * kotlin.math.cos(angle3).toFloat()
                    val innerY2 = centerY + outerRadius * kotlin.math.sin(angle3).toFloat()
                    
                    if (i == 0) {
                        moveTo(outerX1, outerY1)
                    } else {
                        lineTo(outerX1, outerY1)
                    }
                    lineTo(innerX1, innerY1)
                    lineTo(outerX2, outerY2)
                    lineTo(innerX2, innerY2)
                }
                close()
            }
            
            // Draw outer gear
            drawPath(path, color = color)
            
            // Draw inner circle (center hole)
            drawCircle(
                color = color,
                radius = innerRadius,
                center = Offset(centerX, centerY)
            )
            
            // Draw white center to create the hole effect
            drawCircle(
                color = Color.White,
                radius = innerRadius * 0.7f,
                center = Offset(centerX, centerY)
            )
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
