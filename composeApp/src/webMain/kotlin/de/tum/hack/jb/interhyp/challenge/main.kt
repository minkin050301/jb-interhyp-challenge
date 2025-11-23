package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import de.tum.hack.jb.interhyp.challenge.di.appModule
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import de.tum.hack.jb.interhyp.challenge.util.LocaleManager
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }
    
    // Initialize LocaleManager from saved preference
    // This must happen before ComposeViewport to ensure locale is ready
    val savedLocale = de.tum.hack.jb.interhyp.challenge.util.getSavedLocale()
    LocaleManager.initLocale(savedLocale)
    
    ComposeViewport {
        AppWithKoin()
    }
}

@Composable
private fun AppWithKoin() {
    val themeViewModel: ThemeViewModel = koinInject()
    App(themeViewModel = themeViewModel)
}