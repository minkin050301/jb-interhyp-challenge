package de.tum.hack.jb.interhyp.challenge.ui.main

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
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
import jb_interhyp_challenge.composeapp.generated.resources.coupon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import de.tum.hack.jb.interhyp.challenge.ui.util.byteArrayToImageBitmap
import de.tum.hack.jb.interhyp.challenge.ui.util.AnimatedGif
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils
import de.tum.hack.jb.interhyp.challenge.util.formatCurrency
import de.tum.hack.jb.interhyp.challenge.util.currentTimeMillis
import de.tum.hack.jb.interhyp.challenge.util.getYear
import de.tum.hack.jb.interhyp.challenge.util.getMonth
import de.tum.hack.jb.interhyp.challenge.util.rememberVibrator
import kotlin.math.floor
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import de.tum.hack.jb.interhyp.challenge.ui.components.VideoPlayer

/**
 * Format number to thousands (e.g., 42321 -> "42k", 180323 -> "180k")
 */
private fun formatToThousands(amount: Double): String {
    val thousands = floor(amount / 1000.0).toInt()
    return "${thousands}k"
}

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
    var pitBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(Unit) {
        neighborhoodBytes = Res.readBytes("drawable/neighborhood.png")
        pitBytes = Res.readBytes("drawable/pit.mp4")

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
            // Stage 1 (Pit) is now a video, so we skip generation for it
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
    
    // Vibration
    val vibrator = rememberVibrator()

    Box(modifier = Modifier.fillMaxSize()) {
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
                        val progress = uiState.savingsProgress.coerceIn(0f, 1f)
                        
                        // Image at the bottom, aligned with navbar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            // Logic for displaying content based on progress percentage
                            when {
                                // 0-25%: Show empty neighborhood (no building)
                                progress < 0.25f -> {
                                    Image(
                                        painter = painterResource(Res.drawable.neighborhood),
                                        contentDescription = "Neighborhood",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                                // 25-50%: Show Pit Video
                                progress < 0.5f -> {
                                    val videoBytes = pitBytes
                                    if (videoBytes != null && videoBytes.isNotEmpty()) {
                                        VideoPlayer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(9f / 16f)
                                                .align(Alignment.BottomCenter),
                                            videoBytes = videoBytes
                                        )
                                    } else {
                                        // Fallback to neighborhood if video not loaded
                                        Image(
                                            painter = painterResource(Res.drawable.neighborhood),
                                            contentDescription = "Neighborhood",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }
                                // 50-75%: Show STAGE_3
                                progress < 0.75f && uiState.buildingStageImages.stage3Walls != null -> {
                                    val bitmap = byteArrayToImageBitmap(ImageUtils.decodeBase64ToImage(uiState.buildingStageImages.stage3Walls!!.base64Data))
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Construction Stage 3",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }
                                // 75-100%: Show STAGE_4
                                progress < 1.0f && uiState.buildingStageImages.stage4Finishing != null -> {
                                    val bitmap = byteArrayToImageBitmap(ImageUtils.decodeBase64ToImage(uiState.buildingStageImages.stage4Finishing!!.base64Data))
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Construction Stage 4",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }
                                // 100%: Show STAGE_5 (Final)
                                progress >= 1.0f && uiState.buildingStageImages.stage5Final != null -> {
                                    val bitmap = byteArrayToImageBitmap(ImageUtils.decodeBase64ToImage(uiState.buildingStageImages.stage5Final!!.base64Data))
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Construction Stage 5 - Final",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }
                                // Fallback: Show neighborhood
                                else -> {
                                    Image(
                                        painter = painterResource(Res.drawable.neighborhood),
                                        contentDescription = "Neighborhood",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth
                                    )

                                    // Show loading indicator while generating
                                    if (!uiState.buildingStageImages.allStagesGenerated() &&
                                        (uiState.isGeneratingImage || uiState.isGeneratingStageImage || uiState.generatedHouseImage != null)) {
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
                        }

                        // Progress bar at the top, over the image
                        val density = LocalDensity.current
                        // Current date display in bottom left
                        val currentTime = uiState.simulationDate ?: currentTimeMillis()
                        val currentYear = getYear(currentTime)
                        val currentMonth = getMonth(currentTime)
                        val monthNames = listOf(
                            "January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"
                        )
                        val monthName = monthNames.getOrElse(currentMonth) { "Unknown" }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(
                                    start = 24.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                                    top = 20.dp
                                )
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentYear.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Play button in bottom right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    end = 24.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                                    top = 20.dp
                                )
                        ) {
                             IconButton(
                                onClick = { dashboardViewModel.toggleSimulation() },
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSimulationPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (uiState.isSimulationPlaying) "Pause" else "Play",
                                    tint = Color.White
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart)
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row with percentage on left and balance/goal on right (above progress bar)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Percentage complete on the left
                                Text(
                                    "${(uiState.savingsProgress * 100).toInt()}% complete",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )

                                // Balance / Goal on the right
                                Text(
                                    "${formatToThousands(uiState.currentSavings)} / ${formatToThousands(uiState.targetSavings)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Custom progress bar with checkpoints
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                            ) {
                                // Progress bar track
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val trackHeight = 14.dp.toPx()
                                    val cornerRadius = 7.dp.toPx()
                                    val trackTop = (size.height - trackHeight) / 2
                                    val trackBottom = trackTop + trackHeight

                                    // Draw track (background) with rounded corners
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.25f),
                                        topLeft = Offset(0f, trackTop),
                                        size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                                    )

                                    // Draw filled progress with rounded corners
                                    val progress = uiState.savingsProgress.coerceIn(0f, 1f)
                                    val progressWidth = size.width * progress
                                    if (progressWidth > 0) {
                                        drawRoundRect(
                                            color = Color.White,
                                            topLeft = Offset(0f, trackTop),
                                            size = androidx.compose.ui.geometry.Size(progressWidth, trackHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                                        )
                                    }

                                    // Draw checkpoint lines at 25%, 50%, 75%
                                    val checkpoints = listOf(0.25f, 0.5f, 0.75f)
                                    checkpoints.forEach { checkpoint ->
                                        val x = size.width * checkpoint
                                        // Draw majestic vertical line extending above and below
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.7f),
                                            start = Offset(x, trackTop - 6.dp.toPx()),
                                            end = Offset(x, trackBottom + 6.dp.toPx()),
                                            strokeWidth = 2.5.dp.toPx()
                                        )
                                    }
                                }
                            }
                            
                            // Annotations below progress bar - coupon images
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                            val checkpoints = listOf(0.25f, 0.5f, 0.75f)
                            val currentProgress = uiState.savingsProgress.coerceIn(0f, 1f)
                            
                            checkpoints.forEach { checkpoint ->
                                val isReached = currentProgress >= checkpoint
                                val xPositionPx = constraints.maxWidth * checkpoint
                                
                                // Load coupon image (static PNG or animated GIF)
                                if (isReached) {
                                    // Vibrate when reached during simulation
                                    LaunchedEffect(checkpoint) {
                                        if (uiState.isSimulationPlaying) {
                                            vibrator.vibrate()
                                        }
                                    }

                                    // Use animated GIF for reached checkpoints
                                    var gifBytesState by remember(checkpoint) { mutableStateOf<ByteArray?>(null) }
                                    LaunchedEffect(checkpoint) {
                                        gifBytesState = try {
                                            Res.readBytes("drawable/coupon_active.gif")
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                    
                                    val gifBytes = gifBytesState
                                    if (gifBytes != null) {
                                        // Use animated GIF composable
                                        AnimatedGif(
                                            gifBytes = gifBytes,
                                            contentDescription = "Active coupon at ${(checkpoint * 100).toInt()}%",
                                            modifier = Modifier
                                                .offset(x = with(density) { (xPositionPx - 22.dp.toPx()).toDp() })
                                                .size(44.dp)
                                                .align(Alignment.TopStart)
                                        )
                                    } else {
                                        // Fallback to static image if GIF loading fails
                                        Image(
                                            painter = painterResource(Res.drawable.coupon),
                                            contentDescription = "Active coupon at ${(checkpoint * 100).toInt()}%",
                                            modifier = Modifier
                                                .offset(x = with(density) { (xPositionPx - 22.dp.toPx()).toDp() })
                                                .size(44.dp)
                                                .align(Alignment.TopStart)
                                        )
                                    }
                                } else {
                                    // Use static PNG for unreached checkpoints
                                    Image(
                                        painter = painterResource(Res.drawable.coupon),
                                        contentDescription = "Coupon at ${(checkpoint * 100).toInt()}%",
                                        modifier = Modifier
                                            .offset(x = with(density) { (xPositionPx - 22.dp.toPx()).toDp() })
                                            .size(44.dp)
                                            .align(Alignment.TopStart)
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}
