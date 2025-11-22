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
            
            // Material Design settings gear icon path
            val path = Path().apply {
                // Settings gear with proper cog teeth
                moveTo(19.14f * scale, 12.94f * scale)
                cubicTo(19.18f * scale, 12.64f * scale, 19.2f * scale, 12.33f * scale, 19.2f * scale, 12f * scale)
                cubicTo(19.2f * scale, 11.68f * scale, 19.18f * scale, 11.36f * scale, 19.13f * scale, 11.06f * scale)
                lineTo(21.16f * scale, 9.48f * scale)
                lineTo(19.16f * scale, 6.48f * scale)
                lineTo(16.9f * scale, 7.28f * scale)
                cubicTo(16.29f * scale, 6.81f * scale, 15.61f * scale, 6.42f * scale, 14.87f * scale, 6.13f * scale)
                lineTo(14.5f * scale, 3.75f * scale)
                lineTo(10.5f * scale, 3.75f * scale)
                lineTo(10.13f * scale, 6.13f * scale)
                cubicTo(9.39f * scale, 6.42f * scale, 8.71f * scale, 6.81f * scale, 8.1f * scale, 7.28f * scale)
                lineTo(5.84f * scale, 6.48f * scale)
                lineTo(3.84f * scale, 9.48f * scale)
                lineTo(5.87f * scale, 11.06f * scale)
                cubicTo(5.82f * scale, 11.36f * scale, 5.8f * scale, 11.68f * scale, 5.8f * scale, 12f * scale)
                cubicTo(5.8f * scale, 12.32f * scale, 5.82f * scale, 12.64f * scale, 5.87f * scale, 12.94f * scale)
                lineTo(3.84f * scale, 14.52f * scale)
                lineTo(5.84f * scale, 17.52f * scale)
                lineTo(8.1f * scale, 16.72f * scale)
                cubicTo(8.71f * scale, 17.19f * scale, 9.39f * scale, 17.58f * scale, 10.13f * scale, 17.87f * scale)
                lineTo(10.5f * scale, 20.25f * scale)
                lineTo(14.5f * scale, 20.25f * scale)
                lineTo(14.87f * scale, 17.87f * scale)
                cubicTo(15.61f * scale, 17.58f * scale, 16.29f * scale, 17.19f * scale, 16.9f * scale, 16.72f * scale)
                lineTo(19.16f * scale, 17.52f * scale)
                lineTo(21.16f * scale, 14.52f * scale)
                close()
                
                // Center circle hole
                moveTo(12.5f * scale, 15f * scale)
                cubicTo(10.84f * scale, 15f * scale, 9.5f * scale, 13.66f * scale, 9.5f * scale, 12f * scale)
                cubicTo(9.5f * scale, 10.34f * scale, 10.84f * scale, 9f * scale, 12.5f * scale, 9f * scale)
                cubicTo(14.16f * scale, 9f * scale, 15.5f * scale, 10.34f * scale, 15.5f * scale, 12f * scale)
                cubicTo(15.5f * scale, 13.66f * scale, 14.16f * scale, 15f * scale, 12.5f * scale, 15f * scale)
                close()
            }
            drawPath(path, color = tint.takeIf { it != Color.Unspecified } ?: Color.Black)
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
