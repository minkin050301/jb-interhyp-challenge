package de.tum.hack.jb.interhyp.challenge.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.hack.jb.interhyp.challenge.presentation.insights.ProjectedSavingsPoint
import de.tum.hack.jb.interhyp.challenge.util.formatCurrency
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

/**
 * Simplified line chart showing historical balances and projected balances until goal is reached
 */
@Composable
fun BalanceTimelineChart(
    projectedSavingsData: List<ProjectedSavingsPoint>,
    requiredDownPayment: Double?,
    monthsToGoal: Int?,
    modifier: Modifier = Modifier
) {
    // Handle edge cases
    if (requiredDownPayment == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = stringResource(Res.string.set_property_goal_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }
    
    if (projectedSavingsData.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = stringResource(Res.string.no_savings_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }
    
    // Separate historical and projected data
    val historicalData = projectedSavingsData.filter { it.monthIndex < 0 }.sortedBy { it.monthIndex }
    val currentData = projectedSavingsData.filter { it.monthIndex == 0 }
    val projectedData = projectedSavingsData.filter { it.monthIndex > 0 }.sortedBy { it.monthIndex }
    
    // Check if goal is already reached
    val currentBalance = currentData.firstOrNull()?.cumulativeSavings ?: projectedSavingsData.firstOrNull()?.cumulativeSavings ?: 0.0
    if (currentBalance >= requiredDownPayment) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.goal_reached_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.current_savings_label, formatCurrency(currentBalance)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(Res.string.required_label, formatCurrency(requiredDownPayment)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        return
    }
    
    // Check if monthly savings is negative
    val currentPoint = currentData.firstOrNull() ?: projectedSavingsData.firstOrNull { it.monthIndex == 0 }
    val nextPoint = projectedData.firstOrNull()
    
    if (currentPoint != null && nextPoint != null) {
        val monthlySavings = nextPoint.cumulativeSavings - currentPoint.cumulativeSavings
        if (monthlySavings <= 0) {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.increase_savings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.expenses_exceed_income),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            return
        }
    }
    
    // Calculate chart dimensions
    val allDataPoints = projectedSavingsData.sortedBy { it.monthIndex }
    val minMonth = allDataPoints.first().monthIndex
    val maxMonth = allDataPoints.last().monthIndex
    val monthRange = maxMonth - minMonth
    
    val maxSavings = max(
        allDataPoints.maxOfOrNull { it.cumulativeSavings } ?: 0.0,
        requiredDownPayment
    )
    val minSavings = allDataPoints.minOfOrNull { it.cumulativeSavings } ?: 0.0
    
    // Add padding to the range for better visualization
    val savingsRange = maxSavings - minSavings
    val paddedMin = max(0.0, minSavings - savingsRange * 0.1)
    val paddedMax = maxSavings + savingsRange * 0.1
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and goal info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.balance_timeline_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (monthsToGoal != null) {
                    Text(
                        text = stringResource(Res.string.goal_in_months, formatMonths(monthsToGoal)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                ChartCanvas(
                    historicalData = historicalData,
                    currentData = currentData,
                    projectedData = projectedData,
                    requiredDownPayment = requiredDownPayment,
                    monthsToGoal = monthsToGoal,
                    minMonth = minMonth,
                    maxMonth = maxMonth,
                    minSavings = paddedMin,
                    maxSavings = paddedMax,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)
                        .padding(top = 16.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val yAxisSteps = 5
                    val savingsRangeForLabels = paddedMax - paddedMin
                    for (i in yAxisSteps downTo 0) {
                        val savingsValue = paddedMin + (savingsRangeForLabels * i / yAxisSteps)
                        Text(
                            text = formatCurrencyShort(savingsValue),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
                
                // X-axis labels - positioned at exact data point locations
                val keyPoints = mutableListOf<Int>()
                
                // Always include start point
                if (allDataPoints.isNotEmpty()) {
                    keyPoints.add(allDataPoints.first().monthIndex)
                }
                
                // Include current month (0) if it's in range
                if (minMonth <= 0 && maxMonth >= 0) {
                    keyPoints.add(0)
                }
                
                // Include goal month if available
                if (monthsToGoal != null && monthsToGoal >= minMonth && monthsToGoal <= maxMonth) {
                    keyPoints.add(monthsToGoal)
                }
                
                // Include end point
                if (allDataPoints.isNotEmpty()) {
                    keyPoints.add(allDataPoints.last().monthIndex)
                }
                
                // Remove duplicates and sort
                val uniqueKeyPoints = keyPoints.distinct().sorted()
                
                // Limit to max 5 labels for readability
                val candidatePoints = if (uniqueKeyPoints.size > 5) {
                    // Take first, middle, and last
                    listOf(
                        uniqueKeyPoints.first(),
                        uniqueKeyPoints[uniqueKeyPoints.size / 2],
                        uniqueKeyPoints.last()
                    )
                } else {
                    uniqueKeyPoints
                }
                
                // Filter out overlapping labels - ensure minimum spacing
                val density = LocalDensity.current
                var labelAreaWidth by remember { mutableStateOf(0.dp) }
                
                // Estimate minimum spacing needed (approximately 60dp for typical label width)
                // Increased to account for longer labels like "1y 5mo ago" or "in 3y 5mo"
                val minSpacingDp = 70.dp
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 60.dp, end = 16.dp, bottom = 4.dp)
                        .height(20.dp)
                        .onGloballyPositioned { coordinates ->
                            labelAreaWidth = with(density) {
                                coordinates.size.width.toDp()
                            }
                        }
                ) {
                    val monthRangeFloat = monthRange.toFloat()
                    
                    // Filter labels to prevent overlap - use actual label widths
                    val displayPoints = if (labelAreaWidth > 0.dp) {
                        val minSpacingRatio = with(density) {
                            minSpacingDp.toPx() / labelAreaWidth.toPx()
                        }
                        val filteredPoints = mutableListOf<Int>()
                        
                        // Pre-calculate label texts and estimated widths for all candidate points
                        val labelInfo = candidatePoints.map { monthValue ->
                            val labelText = formatMonthLabel(monthValue)
                            // More accurate width estimate: 7dp per character for 10sp font
                            val estimatedWidth = with(density) {
                                (labelText.length * 7.dp.toPx()).toDp()
                            }
                            val halfWidth = estimatedWidth / 2
                            val position = ((monthValue - minMonth).toFloat() / monthRangeFloat.coerceAtLeast(1f))
                            val baseX = with(density) {
                                (position * labelAreaWidth.toPx()).toDp()
                            }
                            // Calculate adjusted position (accounting for edge adjustments)
                            val adjustedX = when {
                                baseX - halfWidth < 0.dp -> halfWidth
                                baseX + halfWidth > labelAreaWidth -> labelAreaWidth - halfWidth
                                else -> baseX
                            }
                            Triple(monthValue, adjustedX, halfWidth)
                        }
                        
                        // Sort by importance: "Now" (0) and goal month are most important
                        val sortedByImportance = labelInfo.sortedBy { (monthValue, _, _) ->
                            when {
                                monthValue == 0 -> 0 // "Now" is most important
                                monthsToGoal != null && monthValue == monthsToGoal -> 1 // Goal month is second
                                else -> 2 // Others are least important
                            }
                        }
                        
                        sortedByImportance.forEach { (monthValue, adjustedX, halfWidth) ->
                            // Check if this label would overlap with any already added label
                            // Compare actual adjusted positions and half-widths
                            val wouldOverlap = filteredPoints.any { existingMonth ->
                                val existingInfo = labelInfo.find { it.first == existingMonth }
                                if (existingInfo != null) {
                                    val (_, existingX, existingHalfWidth) = existingInfo
                                    // Check if the label bounds overlap (considering both half-widths)
                                    val adjustedXPx = with(density) { adjustedX.toPx() }
                                    val existingXPx = with(density) { existingX.toPx() }
                                    val halfWidthPx = with(density) { halfWidth.toPx() }
                                    val existingHalfWidthPx = with(density) { existingHalfWidth.toPx() }
                                    val distance = kotlin.math.abs(adjustedXPx - existingXPx)
                                    val minRequiredDistance = (halfWidthPx + existingHalfWidthPx) * 1.1f // Add 10% buffer
                                    distance < minRequiredDistance
                                } else {
                                    false
                                }
                            }
                            
                            if (!wouldOverlap) {
                                filteredPoints.add(monthValue)
                            }
                        }
                        filteredPoints.sorted()
                    } else {
                        candidatePoints
                    }
                    
                    displayPoints.forEach { monthValue ->
                        // Calculate position matching the Canvas calculation
                        val position = ((monthValue - minMonth).toFloat() / monthRangeFloat.coerceAtLeast(1f))
                        val labelText = formatMonthLabel(monthValue)
                        
                        // Estimate label width (approximately 7dp per character for 10sp font)
                        val estimatedLabelWidth = with(density) {
                            (labelText.length * 7.dp.toPx()).toDp()
                        }
                        val halfLabelWidth = estimatedLabelWidth / 2
                        
                        // Calculate base x position
                        val baseX = with(density) {
                            (position * labelAreaWidth.toPx()).toDp()
                        }
                        
                        // Check if this is the leftmost point (first historical month)
                        val isLeftmost = monthValue == minMonth
                        val isRightmost = monthValue == maxMonth
                        
                        // Adjust position to prevent truncation at edges and align leftmost label
                        val adjustedX = when {
                            // Leftmost label: left-align with graph start
                            isLeftmost -> {
                                // Ensure it doesn't go off the left edge
                                maxOf(0.dp, baseX)
                            }
                            // Rightmost label: right-align with graph end
                            isRightmost -> {
                                // Ensure it doesn't go off the right edge
                                minOf(labelAreaWidth - estimatedLabelWidth, baseX - estimatedLabelWidth)
                            }
                            // Too close to left edge - shift right
                            baseX - halfLabelWidth < 0.dp -> halfLabelWidth
                            // Too close to right edge - shift left
                            baseX + halfLabelWidth > labelAreaWidth -> labelAreaWidth - halfLabelWidth
                            // Otherwise use base position (centered)
                            else -> baseX
                        }
                        
                        Box(
                            modifier = Modifier.offset(x = adjustedX),
                            contentAlignment = if (isLeftmost) Alignment.CenterStart else if (isRightmost) Alignment.CenterEnd else Alignment.Center
                        ) {
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            
            // Simplified legend - only show if both historical and projected data exist
            if (historicalData.isNotEmpty() && projectedData.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = stringResource(Res.string.historical)
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = stringResource(Res.string.projected),
                        isDashed = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCanvas(
    historicalData: List<ProjectedSavingsPoint>,
    currentData: List<ProjectedSavingsPoint>,
    projectedData: List<ProjectedSavingsPoint>,
    requiredDownPayment: Double,
    monthsToGoal: Int?,
    minMonth: Int,
    maxMonth: Int,
    minSavings: Double,
    maxSavings: Double,
    modifier: Modifier = Modifier
) {
    // Get colors before entering Canvas drawScope
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    // Use tertiary color for projected data to differentiate from historical
    val projectedColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier) {
        val paddingLeft = 60.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 40.dp.toPx()
        
        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom
        
        val savingsRange = maxSavings - minSavings
        val savingsRangeFloat = savingsRange.toFloat()
        val minSavingsFloat = minSavings.toFloat()
        val savingsScale = if (savingsRange > 0) chartHeight / savingsRangeFloat else 1f
        
        val monthRange = (maxMonth - minMonth).coerceAtLeast(1).toFloat()
        
        // Helper function to convert month index and savings to coordinates
        fun getCoordinates(monthIndex: Int, savings: Double): Offset {
            val x = paddingLeft + (chartWidth * (monthIndex - minMonth).toFloat() / monthRange)
            val y = paddingTop + chartHeight - ((savings.toFloat() - minSavingsFloat) * savingsScale)
            return Offset(x, y)
        }
        
        // Draw grid lines
        val yAxisSteps = 5
        for (i in 0..yAxisSteps) {
            val savingsValue = minSavingsFloat + (savingsRangeFloat * i / yAxisSteps)
            val y = paddingTop + chartHeight - ((savingsValue - minSavingsFloat) * savingsScale)
            
            drawLine(
                color = outlineVariantColor.copy(alpha = 0.3f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw vertical line at month 0 (current month) to separate historical from projected
        if (minMonth < 0 && maxMonth > 0) {
            val currentMonthX = paddingLeft + (chartWidth * (-minMonth).toFloat() / monthRange)
            drawLine(
                color = outlineColor.copy(alpha = 0.5f),
                start = Offset(currentMonthX, paddingTop),
                end = Offset(currentMonthX, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw required down payment line
        val goalY = paddingTop + chartHeight - ((requiredDownPayment.toFloat() - minSavingsFloat) * savingsScale)
        drawLine(
            color = secondaryColor,
            start = Offset(paddingLeft, goalY),
            end = Offset(paddingLeft + chartWidth, goalY),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw historical line (solid)
        if (historicalData.isNotEmpty()) {
            val historicalPath = Path()
            val allHistorical = (historicalData + currentData).sortedBy { it.monthIndex }
            
            if (allHistorical.isNotEmpty()) {
                val firstPoint = getCoordinates(allHistorical.first().monthIndex, allHistorical.first().cumulativeSavings)
                historicalPath.moveTo(firstPoint.x, firstPoint.y)
                
                for (i in 1 until allHistorical.size) {
                    val point = allHistorical[i]
                    val coords = getCoordinates(point.monthIndex, point.cumulativeSavings)
                    historicalPath.lineTo(coords.x, coords.y)
                }
                
                drawPath(
                    path = historicalPath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                
                // Draw historical data points
                for (point in allHistorical) {
                    val coords = getCoordinates(point.monthIndex, point.cumulativeSavings)
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = coords
                    )
                }
            }
        }
        
        // Draw projected line (dashed)
        if (projectedData.isNotEmpty()) {
            val projectedPath = Path()
            val allProjected = (currentData + projectedData).sortedBy { it.monthIndex }
            
            if (allProjected.isNotEmpty()) {
                val firstPoint = getCoordinates(allProjected.first().monthIndex, allProjected.first().cumulativeSavings)
                projectedPath.moveTo(firstPoint.x, firstPoint.y)
                
                for (i in 1 until allProjected.size) {
                    val point = allProjected[i]
                    val coords = getCoordinates(point.monthIndex, point.cumulativeSavings)
                    projectedPath.lineTo(coords.x, coords.y)
                }
                
                drawPath(
                    path = projectedPath,
                    color = projectedColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                )
                
                // Draw projected data points with different color
                for (point in allProjected) {
                    val coords = getCoordinates(point.monthIndex, point.cumulativeSavings)
                    drawCircle(
                        color = projectedColor,
                        radius = 4.dp.toPx(),
                        center = coords
                    )
                }
            }
        }
        
        // Draw intersection point if goal is reached
        if (monthsToGoal != null && monthsToGoal <= maxMonth) {
            val intersectionX = paddingLeft + (chartWidth * (monthsToGoal - minMonth).toFloat() / monthRange)
            drawCircle(
                color = tertiaryColor,
                radius = 8.dp.toPx(),
                center = Offset(intersectionX, goalY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(intersectionX, goalY)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isDashed: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDashed) {
            // Draw a small dashed line for legend
            Canvas(modifier = Modifier.size(16.dp, 2.dp)) {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 2f))
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun formatMonths(months: Int): String {
    return when {
        months == 0 -> stringResource(Res.string.now)
        months < 0 -> {
            val absMonths = -months
            when {
                absMonths < 12 -> stringResource(Res.string.months_ago, absMonths)
                absMonths % 12 == 0 -> stringResource(Res.string.years_ago, absMonths / 12)
                else -> stringResource(Res.string.years_months_ago, absMonths / 12, absMonths % 12)
            }
        }
        months < 12 -> stringResource(Res.string.in_months, months)
        months % 12 == 0 -> stringResource(Res.string.in_years, months / 12)
        else -> stringResource(Res.string.in_years_months, months / 12, months % 12)
    }
}

@Composable
private fun formatMonthLabel(monthIndex: Int): String {
    return when {
        monthIndex == 0 -> stringResource(Res.string.now)
        monthIndex < 0 -> {
            val absMonths = -monthIndex
            when {
                absMonths < 12 -> stringResource(Res.string.months_ago, absMonths)
                absMonths % 12 == 0 -> stringResource(Res.string.years_ago, absMonths / 12)
                else -> stringResource(Res.string.years_months_ago, absMonths / 12, absMonths % 12)
            }
        }
        else -> {
            when {
                monthIndex < 12 -> stringResource(Res.string.in_months, monthIndex)
                monthIndex % 12 == 0 -> stringResource(Res.string.in_years, monthIndex / 12)
                else -> stringResource(Res.string.in_years_months, monthIndex / 12, monthIndex % 12)
            }
        }
    }
}

private fun formatCurrencyShort(amount: Double): String {
    return when {
        amount >= 1_000_000 -> {
            val millions = amount / 1_000_000
            val rounded = (millions * 10).toInt() / 10.0
            "${rounded}M"
        }
        amount >= 1_000 -> {
            val thousands = amount / 1_000
            val rounded = (thousands * 10).toInt() / 10.0
            "${rounded}K"
        }
        else -> "${amount.toInt()}"
    }
}

