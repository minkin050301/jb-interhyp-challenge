package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import de.tum.hack.jb.interhyp.challenge.di.appModule
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }
    
    ComposeViewport {
        AppWithKoin()
    }
}

@Composable
private fun AppWithKoin() {
    val themeViewModel: ThemeViewModel = koinInject()
    App(themeViewModel = themeViewModel)
}