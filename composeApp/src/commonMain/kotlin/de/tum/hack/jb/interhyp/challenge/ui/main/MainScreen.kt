package de.tum.hack.jb.interhyp.challenge.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings
import de.tum.hack.jb.interhyp.challenge.ui.insights.InsightsScreen
import de.tum.hack.jb.interhyp.challenge.ui.settings.SettingsScreen
import de.tum.hack.jb.interhyp.challenge.ui.profile.ProfileEditScreen
import de.tum.hack.jb.interhyp.challenge.presentation.insights.InsightsViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen(themeViewModel: ThemeViewModel) {
    // Placeholder progress; in future wire to persisted user data / ViewModel StateFlow
    val progress = 0.35f
    
    // Simple state-based navigation
    var currentScreen by remember { mutableStateOf<String?>("home") }

    // Inject InsightsViewModel
    val insightsViewModel: InsightsViewModel = koinInject()

    when (currentScreen) {
        "insights" -> {
            InsightsScreen(
                viewModel = insightsViewModel,
                onNavigate = { screenId -> currentScreen = screenId }
            )
        }
        "settings" -> {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onNavigate = { screenId -> currentScreen = screenId },
                onNavigateToProfile = { currentScreen = "profile" }
            )
        }
        "profile" -> {
            ProfileEditScreen(
                onBack = { currentScreen = "settings" }
            )
        }
        else -> {
            AppScaffold(
                navItemsLeft = listOf(NavItem(id = "insights", label = "Insights", icon = Insights)),
                navItemsRight = listOf(NavItem(id = "settings", label = "Settings", icon = Settings)),
                selectedItemId = currentScreen ?: "home",
                onItemSelected = { id ->
                    currentScreen = id
                },
                onHomeClick = { currentScreen = "home" }
            ) { innerPadding: PaddingValues ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Dashboard", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Your savings progress", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("${(progress * 100).toInt()}% complete", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
