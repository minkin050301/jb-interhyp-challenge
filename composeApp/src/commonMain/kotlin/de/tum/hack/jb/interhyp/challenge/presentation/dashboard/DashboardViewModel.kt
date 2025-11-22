package de.tum.hack.jb.interhyp.challenge.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetCalculation
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.BudgetTrackingRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.PropertyRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import de.tum.hack.jb.interhyp.challenge.domain.model.User
import de.tum.hack.jb.interhyp.challenge.domain.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * House building state enum
 */
enum class HouseState {
    FOUNDATION,  // 0-33%
    WALLS,       // 34-66%
    ROOF         // 67-100%
}

/**
 * UI State for Dashboard screen
 */
data class DashboardUiState(
    val user: User? = null,
    val currentSavings: Double = 0.0,
    val targetSavings: Double = 0.0,
    val savingsProgress: Float = 0.0f,
    val houseState: HouseState = HouseState.FOUNDATION,
    val budgetCalculation: BudgetCalculation? = null,
    val propertyPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Calculate house state based on savings progress
     */
    fun calculateHouseState(): HouseState {
        return when {
            savingsProgress < 0.33f -> HouseState.FOUNDATION
            savingsProgress < 0.67f -> HouseState.WALLS
            else -> HouseState.ROOF
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
}
