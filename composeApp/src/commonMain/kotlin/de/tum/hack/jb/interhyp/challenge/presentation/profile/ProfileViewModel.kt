package de.tum.hack.jb.interhyp.challenge.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Profile Edit screen
 */
data class ProfileUiState(
    val name: String = "",
    val age: Int = 25,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val currentEquity: Double = 0.0,
    val desiredLocation: String = "Munich",
    val desiredPropertySize: Double = 80.0,
    val desiredPropertyType: PropertyType = PropertyType.APARTMENT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val hasExistingProfile: Boolean = false
)

/**
 * ViewModel for Profile Edit screen
 * Handles loading and updating user profile data
 */
class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    /**
     * Load existing user profile from repository
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // First try to load from partial profile (most recent data)
                userRepository.getPartialProfile().collect { profile ->
                    if (profile != null) {
                        _uiState.update {
                            it.copy(
                                name = profile.name,
                                age = profile.age,
                                monthlyIncome = profile.monthlyIncome,
                                monthlyExpenses = profile.monthlyExpenses,
                                currentEquity = profile.currentEquity,
                                desiredLocation = profile.desiredLocation,
                                desiredPropertySize = profile.desiredPropertySize,
                                desiredPropertyType = profile.desiredPropertyType,
                                isLoading = false,
                                hasExistingProfile = true
                            )
                        }
                    } else {
                        // Fall back to full user profile if no partial profile exists
                        userRepository.getUser().collect { user ->
                            if (user != null) {
                                _uiState.update {
                                    it.copy(
                                        name = user.name,
                                        age = user.age,
                                        monthlyIncome = user.netIncome,
                                        monthlyExpenses = user.expenses,
                                        currentEquity = user.wealth,
                                        isLoading = false,
                                        hasExistingProfile = true
                                    )
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, hasExistingProfile = false) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load profile: ${e.message}")
                }
            }
        }
    }
    
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
     * Save profile changes
     */
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSaved = false) }
            
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
                    monthlyExpenses = state.monthlyExpenses,
                    currentEquity = state.currentEquity,
                    desiredLocation = state.desiredLocation,
                    desiredPropertySize = state.desiredPropertySize,
                    desiredPropertyType = state.desiredPropertyType
                )
                
                // Save both user and partial profile for consistency
                val user = userProfile.toUser()
                userRepository.saveUser(user)
                userRepository.savePartialProfile(userProfile)
                
                _uiState.update { it.copy(isLoading = false, isSaved = true, hasExistingProfile = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = "Failed to save profile: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Reset saved flag (for showing success message)
     */
    fun resetSavedFlag() {
        _uiState.update { it.copy(isSaved = false) }
    }
    
    /**
     * Generate a unique user ID
     */
    private fun generateUserId(): String {
        return "user_${kotlin.random.Random.nextLong()}"
    }
}
