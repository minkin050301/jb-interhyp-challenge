package de.tum.hack.jb.interhyp.challenge.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings
import de.tum.hack.jb.interhyp.challenge.ui.insights.InsightsScreen
import de.tum.hack.jb.interhyp.challenge.ui.settings.SettingsScreen
import de.tum.hack.jb.interhyp.challenge.ui.profile.ProfileEditScreen
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.HouseState
import de.tum.hack.jb.interhyp.challenge.presentation.insights.InsightsViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.house
import jb_interhyp_challenge.composeapp.generated.resources.neighborhood
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.util.formatCurrency

import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainScreen(themeViewModel: ThemeViewModel) {
    // Inject DashboardViewModel - image generation is now started in App.kt during onboarding
    val dashboardViewModel: DashboardViewModel = koinInject()
    val vertexAIRepository: VertexAIRepository = koinInject()
    val userRepository: UserRepository = koinInject()
    val httpClient: HttpClient = koinInject()
    
    val uiState by dashboardViewModel.uiState.collectAsState()
    
    // Load resources for stage image generation
    var neighborhoodBytes by remember { mutableStateOf<ByteArray?>(null) }
    var houseBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(Unit) {
        neighborhoodBytes = Res.readBytes("drawable/neighborhood.png")
        
        // Get user's house image if available
        val user = userRepository.getUser().first()
        val houseUrl = user?.goalPropertyImageUrl
        
        houseBytes = if (houseUrl != null) {
            try {
                withContext(Dispatchers.Default) {
                    httpClient.get(houseUrl).body<ByteArray>()
                }
            } catch (e: Exception) {
                Res.readBytes("drawable/house.png")
            }
        } else {
            Res.readBytes("drawable/house.png")
        }
    }
    
    // Automatically generate all 4 building stages in parallel after main composite image is ready
    LaunchedEffect(uiState.generatedHouseImage, neighborhoodBytes, houseBytes) {
        if (uiState.generatedHouseImage != null && 
            neighborhoodBytes != null && 
            houseBytes != null) {
            val stages = uiState.buildingStageImages
            
            // Check if any stages need to be generated and launch them in parallel
            if (stages.stage1Foundation == null) {
                dashboardViewModel.generateBuildingStageImage(
                    vertexAIRepository,
                    neighborhoodBytes!!,
                    houseBytes!!,
                    stage = 1
                )
            }
            if (stages.stage2Frame == null) {
                dashboardViewModel.generateBuildingStageImage(
                    vertexAIRepository,
                    neighborhoodBytes!!,
                    houseBytes!!,
                    stage = 2
                )
            }
            if (stages.stage3Walls == null) {
                dashboardViewModel.generateBuildingStageImage(
                    vertexAIRepository,
                    neighborhoodBytes!!,
                    houseBytes!!,
                    stage = 3
                )
            }
            if (stages.stage4Finishing == null) {
                dashboardViewModel.generateBuildingStageImage(
                    vertexAIRepository,
                    neighborhoodBytes!!,
                    houseBytes!!,
                    stage = 4
                )
            }
        }
    }

    // Simple state-based navigation
    var currentScreen by remember { mutableStateOf<String?>("home") }

    // Inject ViewModels
    val insightsViewModel: InsightsViewModel = koinInject()

    when (currentScreen) {
        "insights" -> {
            InsightsScreen(
                viewModel = insightsViewModel,
                onNavigate = { screenId -> currentScreen = screenId }
            )
        }
        "settings" -> {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onNavigate = { screenId -> currentScreen = screenId },
                onNavigateToProfile = { currentScreen = "profile" }
            )
        }
        "profile" -> {
            ProfileEditScreen(
                onBack = { currentScreen = "settings" }
            )
        }
        else -> {
            AppScaffold(
                navItemsLeft = listOf(NavItem(id = "insights", label = "Insights", icon = Insights)),
                navItemsRight = listOf(NavItem(id = "settings", label = "Settings", icon = Settings)),
                selectedItemId = currentScreen ?: "home",
                onItemSelected = { id ->
                    currentScreen = id
                },
                onHomeClick = { currentScreen = "home" },
                containerColor = Color(0xFFA2C9E8) // Blue background for status bar/dynamic island area
            ) { innerPadding: PaddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFA2C9E8)) // Blue background for top and bottom
                ) {
                    // Image at the bottom, aligned with navbar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                    ) {
                        if (uiState.buildingStageImages.allStagesGenerated()) {
                            // Determine which image to show based on house state
                            val stageImage = when (uiState.houseState) {
                                HouseState.STAGE_1 -> uiState.buildingStageImages.stage1Foundation
                                HouseState.STAGE_2 -> uiState.buildingStageImages.stage2Frame
                                HouseState.STAGE_3 -> uiState.buildingStageImages.stage3Walls
                                HouseState.STAGE_4 -> uiState.buildingStageImages.stage4Finishing
                                HouseState.STAGE_5 -> uiState.buildingStageImages.stage5Final
                            }
                            
                            if (stageImage != null) {
                                val bitmap = byteArrayToImageBitmap(ImageUtils.decodeBase64ToImage(stageImage.base64Data))
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Construction Stage: ${uiState.houseState}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {
                            // Show only neighborhood until all stages are generated
                            Image(
                                painter = painterResource(Res.drawable.neighborhood),
                                contentDescription = "Neighborhood",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth
                            )
                            
                            // Show loading indicator while generating
                            if (uiState.isGeneratingImage || uiState.isGeneratingStageImage || uiState.generatedHouseImage != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    Text(
                                        text = if (uiState.generatedHouseImage == null) "Designing your house..." else "Planning construction stages...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            shape = MaterialTheme.shapes.small
                                        ).padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Progress bar at the top, over the image
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Display current balance and target down payment
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Current Balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    formatCurrency(uiState.currentSavings),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Down Payment Goal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    formatCurrency(uiState.targetSavings),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { uiState.savingsProgress.coerceIn(0f, 1f) }, 
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        
                        // Percentage complete
                        Text(
                            "${(uiState.savingsProgress * 100).toInt()}% complete",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
