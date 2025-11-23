package de.tum.hack.jb.interhyp.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import org.jetbrains.compose.ui.tooling.preview.Preview
import de.tum.hack.jb.interhyp.challenge.ui.theme.AppTheme
import de.tum.hack.jb.interhyp.challenge.ui.main.MainScreen
import de.tum.hack.jb.interhyp.challenge.ui.onboarding.OnboardingScreen
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import de.tum.hack.jb.interhyp.challenge.util.LocaleManager
import de.tum.hack.jb.interhyp.challenge.util.ProvideAppLocale
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import jb_interhyp_challenge.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App(themeViewModel: ThemeViewModel? = null) {
    // Use provided viewModel or create a new instance for previews
    // In production, Koin will be initialized and koinInject will work
    val viewModel = themeViewModel ?: remember { ThemeViewModel() }
    val scope = rememberCoroutineScope()
    
    val themePreference by viewModel.themePreference.collectAsState()
    val currentLocale by LocaleManager.currentLocale.collectAsState()
    
    // Pre-inject dependencies but don't collect state yet to avoid blocking
    val dashboardViewModel: DashboardViewModel = koinInject()
    val vertexAIRepository: VertexAIRepository = koinInject()
    val userRepository: UserRepository = koinInject()
    val httpClient: HttpClient = koinInject()
    
    // Preserve navigation state across locale changes
    var showMain by remember { mutableStateOf(false) }
    var currentMainScreen by remember { mutableStateOf("home") }
    
    // Start background image loading and generation (fire and forget, non-blocking)
    LaunchedEffect(Unit) {
        try {
            // Load resources in background using IO dispatcher
            val neighborhoodBytes = withContext(Dispatchers.Default) {
                Res.readBytes("drawable/neighborhood.png")
            }
            
            // Check for user profile and selected house
            val user = userRepository.getUser().first()
            val houseUrl = user?.goalPropertyImageUrl
            
            val houseBytes = if (houseUrl != null) {
                try {
                    withContext(Dispatchers.Default) {
                        httpClient.get(houseUrl).body<ByteArray>()
                    }
                } catch (e: Exception) {
                    println("Failed to load user house image: ${e.message}")
                    withContext(Dispatchers.Default) {
                        Res.readBytes("drawable/house.png")
                    }
                }
            } else {
                withContext(Dispatchers.Default) {
                    Res.readBytes("drawable/house.png")
                }
            }
            
            // Start image generation in background (non-blocking)
            dashboardViewModel.generateCompositeImage(vertexAIRepository, neighborhoodBytes, houseBytes)
        } catch (e: Exception) {
            println("Background image loading failed: ${e.message}")
        }
    }
    
    // Use key() to force recomposition when locale changes
    key(currentLocale) {
        ProvideAppLocale(locale = currentLocale) {
            AppTheme(themePreference = themePreference) {
                if (showMain) {
                    MainScreen(
                        themeViewModel = viewModel,
                        currentScreenState = currentMainScreen,
                        onScreenChange = { currentMainScreen = it }
                    )
                } else {
                    OnboardingScreen(
                        onSkip = { showMain = true },
                        onComplete = { 
                            // Re-generate with new house selection before showing main screen
                            scope.launch {
                                try {
                                    val neighborhoodBytes = withContext(Dispatchers.Default) {
                                        Res.readBytes("drawable/neighborhood.png")
                                    }
                                    val user = userRepository.getUser().first()
                                    val houseUrl = user?.goalPropertyImageUrl
                                    
                                    val houseBytes = if (houseUrl != null) {
                                        try {
                                            withContext(Dispatchers.Default) {
                                                httpClient.get(houseUrl).body<ByteArray>()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Default) {
                                                Res.readBytes("drawable/house.png")
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Default) {
                                            Res.readBytes("drawable/house.png")
                                        }
                                    }
                                    
                                    dashboardViewModel.generateCompositeImage(vertexAIRepository, neighborhoodBytes, houseBytes)
                                } catch(e: Exception) {
                                    println("Error refreshing house image: ${e.message}")
                                }
                                showMain = true 
                            }
                        }
                    )
                }
            }
        }
    }
}