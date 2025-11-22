package de.tum.hack.jb.interhyp.challenge

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import de.tum.hack.jb.interhyp.challenge.di.appModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin before composing
    startKoin {
        modules(appModule)
    }
    
    ComposeViewport {
        App()
    }
}