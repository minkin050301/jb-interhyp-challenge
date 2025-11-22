package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.ui.tooling.preview.Preview
import de.tum.hack.jb.interhyp.challenge.ui.theme.AppTheme
import de.tum.hack.jb.interhyp.challenge.ui.main.MainScreen
import de.tum.hack.jb.interhyp.challenge.ui.onboarding.OnboardingScreen
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import org.koin.compose.koinInject

@Composable
@Preview
fun App(themeViewModel: ThemeViewModel? = null) {
    // Use provided viewModel or create a new instance for previews
    // In production, Koin will be initialized and koinInject will work
    val viewModel = themeViewModel ?: remember { ThemeViewModel() }
    
    val themePreference by viewModel.themePreference.collectAsState()
    
    AppTheme(themePreference = themePreference) {
        var showMain by remember { mutableStateOf(false) }

        if (showMain) {
            MainScreen(themeViewModel = viewModel)
        } else {
            OnboardingScreen(onSkip = { showMain = true })
        }
    }
}