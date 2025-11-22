package de.tum.hack.jb.interhyp.challenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.tum.hack.jb.interhyp.challenge.di.appModule
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import de.tum.hack.jb.interhyp.challenge.util.initLocaleManager
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize Koin
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }
        
        // Initialize LocaleManager for runtime locale switching
        initLocaleManager(this)

        setContent {
            AppWithKoin()
        }
    }
}

@Composable
private fun AppWithKoin() {
    val themeViewModel: ThemeViewModel = koinInject()
    App(themeViewModel = themeViewModel)
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}