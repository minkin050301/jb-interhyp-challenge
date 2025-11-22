package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.ui.tooling.preview.Preview
import de.tum.hack.jb.interhyp.challenge.ui.theme.AppTheme
import de.tum.hack.jb.interhyp.challenge.ui.main.MainScreen
import de.tum.hack.jb.interhyp.challenge.ui.onboarding.OnboardingScreen

@Composable
@Preview
fun App() {
    AppTheme {
        var showMain by remember { mutableStateOf(false) }

        if (showMain) {
            MainScreen()
        } else {
            OnboardingScreen(
                onSkip = { showMain = true },
                onComplete = { showMain = true }
            )
        }
    }
}