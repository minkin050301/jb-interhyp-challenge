package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import de.tum.hack.jb.interhyp.challenge.di.appModule
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }
    AppWithKoin()
}

@Composable
private fun AppWithKoin() {
    val themeViewModel: ThemeViewModel = koinInject()
    App(themeViewModel = themeViewModel)
}