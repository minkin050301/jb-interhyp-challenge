package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import jb_interhyp_challenge.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App(themeViewModel: ThemeViewModel? = null) {
    // Use provided viewModel or create a new instance for previews
    // In production, Koin will be initialized and koinInject will work
    val viewModel = themeViewModel ?: remember { ThemeViewModel() }
    
    val themePreference by viewModel.themePreference.collectAsState()
    
    // Pre-inject dependencies but don't collect state yet to avoid blocking
    val dashboardViewModel: DashboardViewModel = koinInject()
    val vertexAIRepository: VertexAIRepository = koinInject()
    
    // Start background image loading and generation (fire and forget, non-blocking)
    LaunchedEffect(Unit) {
        try {
            // Load resources in background using IO dispatcher
            val neighborhoodBytes = withContext(Dispatchers.Default) {
                Res.readBytes("drawable/neighborhood.png")
            }
            val houseBytes = withContext(Dispatchers.Default) {
                Res.readBytes("drawable/house.png")
            }
            
            // Start image generation in background (non-blocking)
            dashboardViewModel.generateCompositeImage(vertexAIRepository, neighborhoodBytes, houseBytes)
        } catch (e: Exception) {
            println("Background image loading failed: ${e.message}")
        }
    }
    
    AppTheme(themePreference = themePreference) {
        var showMain by remember { mutableStateOf(false) }

        if (showMain) {
            MainScreen(themeViewModel = viewModel)
        } else {
            OnboardingScreen(
                onSkip = { showMain = true },
                onComplete = { showMain = true }
            )
        }
    }
}