package de.tum.hack.jb.interhyp.challenge

import androidx.compose.ui.window.ComposeUIViewController
import de.tum.hack.jb.interhyp.challenge.di.appModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    // Initialize Koin before composing
    try {
        startKoin {
            modules(appModule)
        }
    } catch (e: Exception) {
        // Koin already started, ignore
    }
    
    App()
}