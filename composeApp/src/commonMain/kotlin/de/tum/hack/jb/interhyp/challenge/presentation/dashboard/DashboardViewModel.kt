package de.tum.hack.jb.interhyp.challenge.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetCalculation
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.PropertyRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.GeneratedImage
import kotlinx.coroutines.flow.first
import de.tum.hack.jb.interhyp.challenge.domain.model.User
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
// import jb_interhyp_challenge.composeapp.generated.resources.Res
// import jb_interhyp_challenge.composeapp.generated.resources.image
// import jb_interhyp_challenge.composeapp.generated.resources.neighborhood

/**
 * House building state enum
 */
enum class HouseState {
    STAGE_1, // 0-20%
    STAGE_2, // 21-40%
    STAGE_3, // 41-60%
    STAGE_4, // 61-80%
    STAGE_5  // 81-100%
}

/**
 * Building stage images storage
 */
data class BuildingStageImages(
    val stage1Foundation: GeneratedImage? = null,
    val stage2Frame: GeneratedImage? = null,
    val stage3Walls: GeneratedImage? = null,
    val stage4Finishing: GeneratedImage? = null,
    val stage5Final: GeneratedImage? = null
) {
    fun allStagesGenerated(): Boolean {
        return stage1Foundation != null && 
               stage2Frame != null && 
               stage3Walls != null && 
               stage4Finishing != null &&
               stage5Final != null
    }
}

/**
 * UI State for Dashboard screen
 */
data class DashboardUiState(
    val user: User? = null,
    val currentSavings: Double = 0.0,
    val targetSavings: Double = 0.0,
    val savingsProgress: Float = 0.0f,
    val houseState: HouseState = HouseState.STAGE_1,
    val budgetCalculation: BudgetCalculation? = null,
    val propertyPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val generatedHouseImage: GeneratedImage? = null,
    val isGeneratingImage: Boolean = false,
    val buildingStageImages: BuildingStageImages = BuildingStageImages(),
    val isGeneratingStageImage: Boolean = false
) {
    /**
     * Calculate house state based on savings progress
     */
    fun calculateHouseState(): HouseState {
        return when {
            savingsProgress <= 0.20f -> HouseState.STAGE_1
            savingsProgress <= 0.40f -> HouseState.STAGE_2
            savingsProgress <= 0.60f -> HouseState.STAGE_3
            savingsProgress <= 0.80f -> HouseState.STAGE_4
            else -> HouseState.STAGE_5
        }
    }
}

/**
 * ViewModel for Dashboard screen
 * Handles house building visualization and budget tracking
 */
class DashboardViewModel(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val budgetRepository: BudgetRepository,
    private val budgetTrackingRepository: BudgetTrackingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    /**
     * Load dashboard data
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            userRepository.getUser().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(user = user) }
                    loadPropertyAndBudget(user)
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = "No user profile found") 
                    }
                }
            }
        }
    }
    
    /**
     * Load property price and calculate budget
     */
    private suspend fun loadPropertyAndBudget(user: User) {
        viewModelScope.launch {
            // Use saved goal price if available, otherwise calculate average
            val propertyPrice = if (user.goalPropertyPrice != null && user.goalPropertyPrice!! > 0) {
                // Use saved goal price
                user.goalPropertyPrice!!
            } else {
                // Fall back to calculating average price
                // Create UserProfile from User for budget calculation
                val userProfile = UserProfile(
                    userId = user.id,
                    name = user.name,
                    age = user.age,
                    monthlyIncome = user.netIncome,
                    monthlyExpenses = user.expenses,
                    currentEquity = user.wealth,
                    desiredLocation = "Munich", // Default, should be stored
                    desiredPropertySize = user.goalPropertySize ?: 80.0  // Use saved goal size or default
                )
                
                // Get average property price - use first() to get a single value
                val result = propertyRepository.getAveragePrice(
                    userProfile.desiredLocation,
                    userProfile.desiredPropertySize
                ).first()
                
                when (result) {
                    is NetworkResult.Success -> result.data
                    is NetworkResult.Error -> {
                        // Use default fallback
                        5000.0 * userProfile.desiredPropertySize
                    }
                    is NetworkResult.Loading -> {
                        // Should not happen with first(), but fallback
                        5000.0 * userProfile.desiredPropertySize
                    }
                }
            }
            
            // Create UserProfile for budget calculation
            val userProfile = UserProfile(
                userId = user.id,
                name = user.name,
                age = user.age,
                monthlyIncome = user.netIncome,
                monthlyExpenses = user.expenses,
                currentEquity = user.wealth,
                desiredLocation = "Munich", // Default, should be stored
                desiredPropertySize = user.goalPropertySize ?: 80.0
            )
            
            _uiState.update { it.copy(propertyPrice = propertyPrice) }
            
            // Calculate budget
            calculateBudget(userProfile, propertyPrice)
        }
    }
    
    /**
     * Calculate budget and update UI state
     */
    private suspend fun calculateBudget(userProfile: UserProfile, propertyPrice: Double) {
        budgetRepository.calculateBudget(userProfile, propertyPrice).collect { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val calculation = result.data
                    
                    // Get bank account balance - it should already reflect user's total wealth
                    // (bank account is synced with user.wealth, so we use bank balance as the source of truth)
                    val bankAccount = budgetTrackingRepository.getBankAccount(userProfile.userId).first()
                    // Use bank account balance if available, otherwise fall back to user wealth
                    val currentSavings = bankAccount?.balance ?: userProfile.currentEquity
                    val targetSavings = calculation.requiredEquity
                    val progress = if (targetSavings > 0) {
                        (currentSavings / targetSavings).toFloat().coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                    
                    _uiState.update {
                        it.copy(
                            currentSavings = currentSavings,
                            targetSavings = targetSavings,
                            savingsProgress = progress,
                            houseState = it.copy(savingsProgress = progress).calculateHouseState(),
                            budgetCalculation = calculation,
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
    
    /**
     * Update user savings
     */
    fun updateSavings(newSavings: Double) {
        viewModelScope.launch {
            _uiState.value.user?.let { user ->
                userRepository.updateWealth(user.id, newSavings)
                loadDashboardData() // Refresh
            }
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refresh() {
        loadDashboardData()
    }
    
    /**
     * Generate composite image using Vertex AI
     * This is a placeholder for now - actual implementation would require injecting VertexAIRepository
     * and reading resources (which needs to happen in the UI/Composable layer or via a ResourceProvider)
     */
    @OptIn(ExperimentalResourceApi::class)
    fun generateCompositeImage(
        vertexAIRepository: VertexAIRepository,
        neighborhoodBytes: ByteArray,
        houseBytes: ByteArray
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingImage = true) }
            
            val prompt = """
                A high-quality 3D app icon of a house rendered in a "claymorphism" style.
                
                Reference Images:
                - First image: The neighborhood background where the house should be placed.
                - Second image: The house design that should be inserted.
                
                Instructions:
                - Create a final image showing the house (from the second image) inserted into the neighborhood (first image).
                - The house should look like it is made of smooth, puffy, matte plastic with rounded edges and corners and soft texture.
                - The house should resemble the second image provided.
                - The lighting should be soft and warm, creating gentle gradients on the surfaces.
                - The design should be cute, minimal, and vibrant.
                - Seamlessly integrate the house into the neighborhood.
            """.trimIndent()
            
            // Encode images to Base64
            val neighborhoodBase64 = de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils.encodeImageToBase64(neighborhoodBytes)
            val houseBase64 = de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils.encodeImageToBase64(houseBytes)
            
            vertexAIRepository.generateImage(
                prompt = prompt,
                inputImages = listOf(
                    neighborhoodBase64 to "image/png",
                    houseBase64 to "image/png"
                ),
                aspectRatio = "9:16"
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val generatedImage = result.data.firstOrNull()
                        _uiState.update { 
                            it.copy(
                                generatedHouseImage = generatedImage,
                                buildingStageImages = it.buildingStageImages.copy(stage5Final = generatedImage),
                                isGeneratingImage = false
                            ) 
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isGeneratingImage = false,
                                errorMessage = "Failed to generate image: ${result.message}"
                            ) 
                        }
                    }
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isGeneratingImage = true) }
                    }
                }
            }
        }
    }
    
    /**
     * Generate building stage image using Vertex AI
     * Combines the neighborhood and house images with a stage-specific prompt
     * This function can be called in parallel for multiple stages
     * 
     * @param vertexAIRepository Repository for Vertex AI operations
     * @param neighborhoodBytes Raw bytes of the neighborhood image
     * @param houseBytes Raw bytes of the house image  
     * @param stage Building stage (1-4)
     */
    @OptIn(ExperimentalResourceApi::class)
    fun generateBuildingStageImage(
        vertexAIRepository: VertexAIRepository,
        neighborhoodBytes: ByteArray,
        houseBytes: ByteArray,
        stage: Int
    ) {
        viewModelScope.launch {
            val prompt = when (stage) {
                1 -> """
                    Edit the house image to show it in the foundation stage.
                    
                    Reference Images:
                    - First image: The neighborhood background.
                    - Second image: The target house design.
                    
                    Instructions:
                    - Show the construction site for the house (from the second image) in the neighborhood (first image).
                    - The house should be integrated naturally into the neighborhood scene.
                    
                    Visual Details:
                    - No parts of the house exist yet
                    - The ground of the property is being dug out
                    - Show excavation work with exposed earth
                    - 3-4 construction workers visible with shovels digging
                    - Workers wearing safety gear (hard hats, high-visibility vests)
                    - Piles of dirt around the excavation site
                    - Construction equipment like wheelbarrows or small machinery
                    - Maintain the claymorphism/3D style consistent with the reference images
                    - Smooth, rounded edges with soft, warm lighting
                    - Seamlessly integrate the construction scene into the provided neighborhood background
                """.trimIndent()
                
                2 -> """
                    Edit the house image to show it in the frame construction stage.
                    
                    Reference Images:
                    - First image: The neighborhood background.
                    - Second image: The target house design.
                    
                    Instructions:
                    - Show the frame of the house (based on the design in the second image) in the neighborhood (first image).
                    - The house should be integrated naturally into the neighborhood scene.
                    
                    Structure Elements:
                    - The wooden or steel skeleton/framework of the house is visible
                    - Vertical wall studs, horizontal floor joists, and roof rafters are clearly defined
                    - The basic shape of walls, floors, and roof structure is apparent (matching the shape of the house in the second image)
                    - NO exterior walls, siding, or roof covering yet - just the bare frame
                    
                    Scaffolding:
                    - Metal or wooden scaffolding surrounds the house frame
                    - Multiple levels of scaffolding platforms with safety railings
                    - Scaffolding extends along the sides and wraps around corners
                    - Realistic construction-grade scaffolding appearance
                    
                    Workers and Activity:
                    - 3-4 construction workers visible on the scaffolding at different heights
                    - Workers wearing safety gear (hard hats, high-visibility vests)
                    - Some workers actively hammering nails into the frame
                    - Workers positioned naturally - some standing, some kneeling while working
                    - Show motion/action suggesting active construction work
                    
                    Additional Construction Details:
                    - Stacks of lumber visible near the base of the structure
                    - A few power tools on the scaffolding platforms
                    - Construction materials organized around the work site
                    - The ground level should show some construction activity/materials
                    
                    Style and Integration:
                    - Maintain the claymorphism/3D style consistent with the reference images
                    - Smooth, rounded edges on both the frame and scaffolding
                    - Soft, warm lighting creating gentle gradients
                    - The house frame should be clearly in Stage 2 of construction (no foundation work visible, but not yet enclosed)
                    - Seamlessly integrate the construction scene into the provided neighborhood background
                    - Keep the overall scene cute, minimal, yet realistic for a construction phase
                    
                    Important:
                    - NO complete walls or roof covering
                    - NO windows or doors yet
                    - Focus on the skeletal framework being assembled
                    - Active construction scene with workers visibly engaged
                """.trimIndent()
                
                3 -> """
                    Edit the house image to show it in the walls and roof stage.
                    
                    Reference Images:
                    - First image: The neighborhood background.
                    - Second image: The target house design.
                    
                    Instructions:
                    - Show the house (based on the design in the second image) with walls and roof in the neighborhood (first image).
                    - The house should be integrated naturally into the neighborhood scene.
                    
                    Visual Details:
                    - Exterior walls are now in place (wood, brick, or other material)
                    - Roof structure is covered with tiles, shingles, or roofing material
                    - The house starts to look "closed" and protected from weather
                    - Basic house shape is complete and matches the second image
                    - Workers adding exterior walls and roof materials
                    - Some scaffolding may still be present
                    - Construction materials and tools visible around the site
                    - Workers wearing safety gear (hard hats, high-visibility vests)
                    - Maintain the claymorphism/3D style consistent with the reference images
                    - Smooth, rounded edges with soft, warm lighting
                    - Seamlessly integrate the construction scene into the provided neighborhood background
                    
                    Important:
                    - NO windows or doors installed yet
                    - NO paint or exterior finishing
                    - Focus on the structural enclosure being completed
                """.trimIndent()
                
                4 -> """
                    Edit the house image to show it in the finishing stage.
                    
                    Reference Images:
                    - First image: The neighborhood background.
                    - Second image: The target house design.
                    
                    Instructions:
                    - Show the house (based on the design in the second image) in the finishing stage in the neighborhood (first image).
                    - The house should be integrated naturally into the neighborhood scene.
                    
                    Visual Details:
                    - Windows and doors are installed (matching the second image)
                    - Exterior paint or siding is applied
                    - Gutters and downspouts are visible
                    - Trim and decorative elements are in place
                    - The house looks nearly complete and polished
                    - Landscaping may be starting (grass, basic plants)
                    - Few or no workers visible - mostly finishing touches
                    - Any remaining scaffolding should be minimal or being removed
                    - Clean construction site with minimal debris
                    - Maintain the claymorphism/3D style consistent with the reference images
                    - Smooth, rounded edges with soft, warm lighting
                    - Seamlessly integrate the finished house into the provided neighborhood background
                    - The house should look move-in ready or very close to it
                """.trimIndent()
                
                else -> throw IllegalArgumentException("Invalid building stage: $stage. Must be 1-4.")
            }
            
            // First, combine the neighborhood and house images
            val neighborhoodBase64 = de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils.encodeImageToBase64(neighborhoodBytes)
            val houseBase64 = de.tum.hack.jb.interhyp.challenge.data.network.ImageUtils.encodeImageToBase64(houseBytes)
            
            // Generate the combined image with the stage-specific prompt
            vertexAIRepository.generateImage(
                prompt = prompt,
                inputImages = listOf(
                    neighborhoodBase64 to "image/png",
                    houseBase64 to "image/png"
                ),
                aspectRatio = "9:16",
                temperature = 0.9
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val generatedImage = result.data.firstOrNull()
                        _uiState.update { currentState ->
                            val updatedStageImages = when (stage) {
                                1 -> currentState.buildingStageImages.copy(stage1Foundation = generatedImage)
                                2 -> currentState.buildingStageImages.copy(stage2Frame = generatedImage)
                                3 -> currentState.buildingStageImages.copy(stage3Walls = generatedImage)
                                4 -> currentState.buildingStageImages.copy(stage4Finishing = generatedImage)
                                else -> currentState.buildingStageImages
                            }
                            
                            currentState.copy(
                                buildingStageImages = updatedStageImages,
                                isGeneratingStageImage = !updatedStageImages.allStagesGenerated()
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                errorMessage = "Failed to generate stage $stage image: ${result.message}"
                            ) 
                        }
                    }
                    is NetworkResult.Loading -> {
                        _uiState.update { it.copy(isGeneratingStageImage = true) }
                    }
                }
            }
        }
    }
}
