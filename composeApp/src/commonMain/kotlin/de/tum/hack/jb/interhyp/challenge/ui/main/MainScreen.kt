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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings

@Composable
fun MainScreen() {
    // Placeholder progress; in future wire to persisted user data / ViewModel StateFlow
    val progress = 0.35f

    AppScaffold(
        navItemsLeft = listOf(NavItem(id = "insights", label = "Insights", icon = Insights)),
        navItemsRight = listOf(NavItem(id = "settings", label = "Settings", icon = Settings)),
        selectedItemId = "home", // show Home as selected by default
        onItemSelected = { /* TODO: hook up Voyager navigation */ },
        onHomeClick = { /* Already on Home; could scroll-to-top or refresh */ }
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
