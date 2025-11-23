package de.tum.hack.jb.interhyp.challenge

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import de.tum.hack.jb.interhyp.challenge.util.applyLocale
import de.tum.hack.jb.interhyp.challenge.util.initLocaleManager
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
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