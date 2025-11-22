package de.tum.hack.jb.interhyp.challenge.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings
import de.tum.hack.jb.interhyp.challenge.ui.theme.ThemePreference
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val themePreference by themeViewModel.themePreference.collectAsState()
    
    AppScaffold(
        navItemsLeft = listOf(NavItem(id = "insights", label = "Insights", icon = Insights)),
        navItemsRight = listOf(NavItem(id = "settings", label = "Settings", icon = Settings)),
        selectedItemId = "settings",
        onItemSelected = { id ->
            if (id != "settings") {
                onNavigateBack()
            }
        },
        onHomeClick = onNavigateBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Appearance Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Theme Selection
                    ThemePreference.values().forEach { preference ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themePreference == preference,
                                onClick = { themeViewModel.setThemePreference(preference) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = preference.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { themeViewModel.setThemePreference(preference) }
                            )
                        }
                    }
                }
            }
        }
    }
}

