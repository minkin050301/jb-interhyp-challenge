package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern, sleek bottom navigation scaffold for KMP using Material 3.
 * Features rounded top corners, elevation, smooth animations, and custom icons.
 *
 * Usage:
 * ```kotlin
 * AppScaffold(
 *   navItemsLeft = listOf(NavItem("insights", label = "Insights", icon = Insights)),
 *   navItemsRight = listOf(NavItem("settings", label = "Settings", icon = Settings)),
 *   selectedItemId = "home",
 *   onItemSelected = { id -> /* navigate */ },
 *   onHomeClick = { /* go to main/home */ }
 * ) { innerPadding ->
 *   // Screen content here, apply innerPadding
 * }
 * ```
 */
@Composable
fun AppScaffold(
    navItemsLeft: List<NavItem> = emptyList(),
    navItemsRight: List<NavItem> = emptyList(),
    selectedItemId: String? = null,
    onItemSelected: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = {
            ModernNavBar(
                navItemsLeft = navItemsLeft,
                navItemsRight = navItemsRight,
                selectedItemId = selectedItemId,
                onItemSelected = onItemSelected,
                onHomeClick = onHomeClick
            )
        },
        containerColor = containerColor
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun ModernNavBar(
    navItemsLeft: List<NavItem>,
    navItemsRight: List<NavItem>,
    selectedItemId: String?,
    onItemSelected: (String) -> Unit,
    onHomeClick: () -> Unit,
) {
    val home = NavItem(id = "home", label = "Home", icon = Home)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(0.dp) // Rectangular shape (no rounded corners)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left items
            navItemsLeft.forEach { item ->
                NavBarButton(
                    item = item,
                    isSelected = item.id == selectedItemId,
                    onClick = { onItemSelected(item.id) }
                )
            }
            
            // Center home item (highlighted)
            NavBarButton(
                item = home,
                isSelected = selectedItemId == home.id,
                onClick = { 
                    onHomeClick()
                    onItemSelected(home.id) 
                },
                isHome = true
            )
            
            // Right items
            navItemsRight.forEach { item ->
                NavBarButton(
                    item = item,
                    isSelected = item.id == selectedItemId,
                    onClick = { onItemSelected(item.id) }
                )
            }
        }
    }
}

@Composable
private fun NavBarButton(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    isHome: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = if (isHome) 64.dp else 56.dp, minHeight = if (isHome) 64.dp else 56.dp)
            .scale(scale)
            .clip(if (isHome) CircleShape else RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = if (isHome) 80.dp else 72.dp)
        ) {
            item.icon.content(
                Modifier.size(if (isHome) 28.dp else 24.dp),
                contentColor,
                if (isHome) 28.dp else 24.dp
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

/**
 * Navigation item data class with custom icon support
 */
data class NavItem(
    val id: String,
    val label: String,
    val icon: IconVector
)
