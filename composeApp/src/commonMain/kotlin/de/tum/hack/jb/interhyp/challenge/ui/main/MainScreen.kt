package de.tum.hack.jb.interhyp.challenge.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import de.tum.hack.jb.interhyp.challenge.ui.components.AppScaffold
import de.tum.hack.jb.interhyp.challenge.ui.components.Insights
import de.tum.hack.jb.interhyp.challenge.ui.components.NavItem
import de.tum.hack.jb.interhyp.challenge.ui.components.Settings
import de.tum.hack.jb.interhyp.challenge.ui.insights.InsightsScreen
import de.tum.hack.jb.interhyp.challenge.ui.settings.SettingsScreen
import de.tum.hack.jb.interhyp.challenge.ui.profile.ProfileEditScreen
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.insights.InsightsViewModel
import de.tum.hack.jb.interhyp.challenge.presentation.theme.ThemeViewModel
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import jb_interhyp_challenge.composeapp.generated.resources.Res
import jb_interhyp_challenge.composeapp.generated.resources.house
import jb_interhyp_challenge.composeapp.generated.resources.neighborhood

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.draw.drawWithContent

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import de.tum.hack.jb.interhyp.challenge.presentation.dashboard.DashboardViewModel
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainScreen(themeViewModel: ThemeViewModel) {
    // Inject DashboardViewModel - image generation is now started in App.kt during onboarding
    val dashboardViewModel: DashboardViewModel = koinInject()
    val vertexAIRepository: VertexAIRepository = koinInject()
    val uiState by dashboardViewModel.uiState.collectAsState()
    
    // Load resources for stage image generation
    var neighborhoodBytes by remember { mutableStateOf<ByteArray?>(null) }
    var houseBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(Unit) {
        neighborhoodBytes = Res.readBytes("drawable/neighborhood.png")
        houseBytes = Res.readBytes("drawable/house.png")
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

    // Placeholder progress; in future wire to persisted user data / ViewModel StateFlow
    val progress = 0.35f
    
    // Simple state-based navigation
    var currentScreen by remember { mutableStateOf<String?>("home") }

    // Inject ViewModels
    val insightsViewModel: InsightsViewModel = koinInject()
    val dashboardViewModel: DashboardViewModel = koinInject()
    val uiState by dashboardViewModel.uiState.collectAsState()

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
                onHomeClick = { currentScreen = "home" }
            ) { innerPadding: PaddingValues ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Dashboard", 
                        style = MaterialTheme.typography.headlineMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    val backgroundColor = MaterialTheme.colorScheme.background
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.generatedHouseImage != null) {
                            // Show the generated composite image
                            val bitmap = byteArrayToImageBitmap(ImageUtils.decodeBase64ToImage(uiState.generatedHouseImage!!.base64Data))
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Generated House in Neighborhood",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        } else {
                            // Show only neighborhood until composite is generated
                            Image(
                                painter = painterResource(Res.drawable.neighborhood),
                                contentDescription = "Neighborhood",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                0f to backgroundColor,
                                                0.15f to Color.Transparent,
                                                0.85f to Color.Transparent,
                                                1f to backgroundColor
                                            )
                                        )
                                    },
                                contentScale = ContentScale.FillWidth
                            )
                            
                            // Show loading indicator while generating
                            if (uiState.isGeneratingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your savings progress", style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Text("${(progress * 100).toInt()}% complete", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
