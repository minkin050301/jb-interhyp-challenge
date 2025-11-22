package de.tum.hack.jb.interhyp.challenge.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.repository.PropertyRepository
import de.tum.hack.jb.interhyp.challenge.data.repository.UserRepository
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyListingDto
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Goal Selection screen
 */
data class GoalSelectionUiState(
    val listings: List<PropertyListingDto> = emptyList(),
    val selectedListing: PropertyListingDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for Goal Selection screen
 * Handles property search and selection
 */
class GoalSelectionViewModel(
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoalSelectionUiState())
    val uiState: StateFlow<GoalSelectionUiState> = _uiState.asStateFlow()
    
    /**
     * Search for properties matching the criteria
     */
    fun searchProperties(location: String, size: Double, propertyType: PropertyType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, listings = emptyList()) }
            
            try {
                // Get the final result (skip Loading state)
                val result = propertyRepository.searchPropertyListings(location, size, propertyType)
                    .first { it !is NetworkResult.Loading }
                
                when (result) {
                    is NetworkResult.Success -> {
                        println("DEBUG: GoalSelectionViewModel - Received ${result.data.size} listings")
                        result.data.forEachIndexed { index, listing ->
                            println("DEBUG: Listing $index - ID: ${listing.id}, Title: ${listing.title}, Price: ${listing.buyingPrice}, Size: ${listing.squareMeter}, Images: ${listing.images?.size ?: 0}")
                        }
                        // Update state (viewModelScope.launch already runs on Main dispatcher)
                        _uiState.update {
                            it.copy(
                                listings = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        println("DEBUG: GoalSelectionViewModel - Updated UI state with ${result.data.size} listings")
                        println("DEBUG: GoalSelectionViewModel - Current state: isLoading=${_uiState.value.isLoading}, listings.size=${_uiState.value.listings.size}, errorMessage=${_uiState.value.errorMessage}")
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Failed to load properties"
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        // Should not happen after first()
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Select a property listing as the goal
     */
    fun selectListing(listing: PropertyListingDto) {
        _uiState.update { it.copy(selectedListing = listing) }
    }
    
    /**
     * Save the selected goal to user profile
     */
    fun saveGoal() {
        viewModelScope.launch {
            val selectedListing = _uiState.value.selectedListing
            if (selectedListing == null) {
                _uiState.update { it.copy(errorMessage = "Please select a property") }
                return@launch
            }
            
            val goalPrice = selectedListing.buyingPrice
            val goalSize = selectedListing.squareMeter
            
            if (goalPrice == null || goalSize == null) {
                _uiState.update { it.copy(errorMessage = "Selected property is missing price or size information") }
                return@launch
            }
            
            // Get current user and update with goal
            val user = userRepository.getUser().first()
            if (user != null) {
                val updatedUser = user.copy(
                    goalPropertyPrice = goalPrice,
                    goalPropertySize = goalSize
                )
                userRepository.saveUser(updatedUser)
            }
        }
    }
}

