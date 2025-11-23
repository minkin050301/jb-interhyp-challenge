package de.tum.hack.jb.interhyp.challenge.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.PropertyRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.VertexAIRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Onboarding screen
 */
data class OnboardingUiState(
    val name: String = "",
    val age: Int = 25,
    val monthlyIncome: Double = 0.0,
    val futureMonthlyIncome: Double? = null,
    val monthlyExpenses: Double = 0.0,
    val currentEquity: Double = 0.0,
    val existingCredits: Double = 0.0,
    val desiredLocation: String = "Munich",
    val desiredPropertySize: Double = 80.0,
    val desiredPropertyType: PropertyType = PropertyType.APARTMENT,
    val selfieBase64: String? = null,
    val targetDate: String? = null,
    val desiredChildren: Int = 0,
    val avatarImage: String? = null,
    val numberOfAdults: Int = 1,
    val numberOfChildren: Int = 0,
    val isLoading: Boolean = false,
    val isGeneratingAvatar: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
    val goalPropertyImageUrl: String? = null
)

/**
 * ViewModel for Onboarding screen
 * Handles user profile data collection using unidirectional data flow
 */
class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val budgetRepository: BudgetRepository,
    private val vertexAIRepository: VertexAIRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    /**
     * Update user name
     */
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }
    
    /**
     * Update user age
     */
    fun updateAge(age: Int) {
        _uiState.update { it.copy(age = age) }
    }
    
    /**
     * Update monthly income
     */
    fun updateMonthlyIncome(income: Double) {
        _uiState.update { it.copy(monthlyIncome = income) }
    }
    
    /**
     * Update monthly expenses
     */
    fun updateMonthlyExpenses(expenses: Double) {
        _uiState.update { it.copy(monthlyExpenses = expenses) }
    }
    
    /**
     * Update current equity
     */
    fun updateCurrentEquity(equity: Double) {
        _uiState.update { it.copy(currentEquity = equity) }
    }
    
    /**
     * Update desired location
     */
    fun updateDesiredLocation(location: String) {
        _uiState.update { it.copy(desiredLocation = location) }
    }
    
    /**
     * Update desired property size
     */
    fun updateDesiredPropertySize(size: Double) {
        _uiState.update { it.copy(desiredPropertySize = size) }
    }
    
    /**
     * Update desired property type
     */
    fun updateDesiredPropertyType(type: PropertyType) {
        _uiState.update { it.copy(desiredPropertyType = type) }
    }
    
    /**
     * Update selfie image (Base64 encoded)
     */
    fun updateSelfie(base64Image: String?) {
        _uiState.update { it.copy(selfieBase64 = base64Image) }
    }
    
    /**
     * Update future monthly income
     */
    fun updateFutureMonthlyIncome(income: Double?) {
        _uiState.update { it.copy(futureMonthlyIncome = income) }
    }
    
    /**
     * Update existing credits
     */
    fun updateExistingCredits(credits: Double) {
        _uiState.update { it.copy(existingCredits = credits) }
    }
    
    /**
     * Update target date
     */
    fun updateTargetDate(date: String?) {
        _uiState.update { it.copy(targetDate = date) }
    }
    
    /**
     * Update desired children
     */
    fun updateDesiredChildren(children: Int) {
        _uiState.update { it.copy(desiredChildren = children) }
    }
    
    /**
     * Update avatar image
     */
    fun updateAvatarImage(image: String?) {
        _uiState.update { it.copy(avatarImage = image) }
    }
    
    /**
     * Update number of adults
     */
    fun updateNumberOfAdults(adults: Int) {
        _uiState.update { it.copy(numberOfAdults = adults) }
    }
    
    /**
     * Update number of children
     */
    fun updateNumberOfChildren(children: Int) {
        _uiState.update { it.copy(numberOfChildren = children) }
    }
    
    /**
     * Update goal property image URL
     */
    fun updateGoalPropertyImage(url: String?) {
        _uiState.update { it.copy(goalPropertyImageUrl = url) }
    }
    
    /**
     * Generate avatar from selfie using Vertex AI
     */
    fun generateAvatar(selfieBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAvatar = true, errorMessage = null) }
            
            try {
                // Simple prompt for avatar generation
                val prompt = "Create a stylized cartoon avatar based on this person's photo. Make it friendly and professional."
                
                // Call Vertex AI to generate avatar
                vertexAIRepository.generateImage(
                    prompt = prompt,
                    inputImageBase64 = selfieBase64,
                    mimeType = "image/jpeg",
                    temperature = 1.0
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            // Keep loading state
                        }
                        is NetworkResult.Success -> {
                            // Get the first generated image
                            val avatarBase64 = result.data.firstOrNull()?.base64Data
                            if (avatarBase64 != null) {
                                _uiState.update { 
                                    it.copy(
                                        avatarImage = avatarBase64,
                                        isGeneratingAvatar = false
                                    )
                                }
                            } else {
                                _uiState.update { 
                                    it.copy(
                                        isGeneratingAvatar = false,
                                        errorMessage = "No avatar image generated"
                                    )
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isGeneratingAvatar = false,
                                    errorMessage = "Failed to generate avatar: ${result.message}"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isGeneratingAvatar = false,
                        errorMessage = "Avatar generation error: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Save intermediate progress (partial profile data)
     */
    fun saveIntermediateProgress() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                
                // Create partial user profile from current state
                val partialProfile = UserProfile(
                    userId = generateUserId(),
                    name = state.name,
                    age = state.age,
                    monthlyIncome = state.monthlyIncome,
                    futureMonthlyIncome = state.futureMonthlyIncome,
                    monthlyExpenses = state.monthlyExpenses,
                    currentEquity = state.currentEquity,
                    existingCredits = state.existingCredits,
                    desiredLocation = state.desiredLocation,
                    desiredPropertySize = state.desiredPropertySize,
                    desiredPropertyType = state.desiredPropertyType,
                    targetDate = state.targetDate,
                    desiredChildren = state.desiredChildren,
                    avatarImage = state.avatarImage,
                    goalPropertyImageUrl = state.goalPropertyImageUrl
                )
                
                // Save partial profile
                userRepository.savePartialProfile(partialProfile)
            } catch (e: Exception) {
                // Silent fail for intermediate saves
                println("Failed to save intermediate progress: ${e.message}")
            }
        }
    }
    
    /**
     * Load saved profile data to resume onboarding
     */
    fun loadSavedProfile() {
        viewModelScope.launch {
            try {
                userRepository.getPartialProfile().collect { profile ->
                    profile?.let {
                        _uiState.update { state ->
                            state.copy(
                                name = it.name,
                                age = it.age,
                                monthlyIncome = it.monthlyIncome,
                                futureMonthlyIncome = it.futureMonthlyIncome,
                                monthlyExpenses = it.monthlyExpenses,
                                currentEquity = it.currentEquity,
                                existingCredits = it.existingCredits,
                                desiredLocation = it.desiredLocation,
                                desiredPropertySize = it.desiredPropertySize,
                                desiredPropertyType = it.desiredPropertyType,
                                targetDate = it.targetDate,
                                desiredChildren = it.desiredChildren,
                                avatarImage = it.avatarImage,
                                goalPropertyImageUrl = it.goalPropertyImageUrl,
                                numberOfAdults = it.familyMembers.count { member -> member.age >= 18 }.coerceAtLeast(1),
                                numberOfChildren = it.familyMembers.count { member -> member.age < 18 }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("Failed to load saved profile: ${e.message}")
            }
        }
    }
    
    /**
     * Submit onboarding data and save user profile
     */
    fun submitOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val state = _uiState.value
                
                // Validate input
                if (state.name.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Name is required") }
                    return@launch
                }
                
                if (state.monthlyIncome <= 0) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Income must be positive") }
                    return@launch
                }
                
                // Create user profile
                val userProfile = UserProfile(
                    userId = generateUserId(),
                    name = state.name,
                    age = state.age,
                    monthlyIncome = state.monthlyIncome,
                    futureMonthlyIncome = state.futureMonthlyIncome,
                    monthlyExpenses = state.monthlyExpenses,
                    currentEquity = state.currentEquity,
                    existingCredits = state.existingCredits,
                    desiredLocation = state.desiredLocation,
                    desiredPropertySize = state.desiredPropertySize,
                    desiredPropertyType = state.desiredPropertyType,
                    targetDate = state.targetDate,
                    desiredChildren = state.desiredChildren,
                    avatarImage = state.avatarImage,
                    goalPropertyImageUrl = state.goalPropertyImageUrl
                )
                
                // Save user with selfie
                val user = userProfile.toUser().copy(image = state.selfieBase64)
                userRepository.saveUser(user)
                
                _uiState.update { it.copy(isLoading = false, isCompleted = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = "Failed to save profile: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Generate a unique user ID
     */
    private fun generateUserId(): String {
        return "user_${kotlin.random.Random.nextLong()}"
    }
}
