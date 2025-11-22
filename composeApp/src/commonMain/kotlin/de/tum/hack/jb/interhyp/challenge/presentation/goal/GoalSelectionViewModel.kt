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
    val isLoadingMore: Boolean = false,
    val hasMoreResults: Boolean = false,
    val totalResults: Int = 0,
    val errorMessage: String? = null,
    val isFallback: Boolean = false // Indicates if current listings are from fallback data
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
    
    // Store current search parameters for loading more
    private var currentLocation: String = ""
    private var currentSize: Double = 0.0
    private var currentPropertyType: PropertyType = PropertyType.APARTMENT
    private var apiOffset: Int = 0 // Track API offset (increments by 50 each time we fetch)
    private var totalAvailableResults: Int = 0
    
    /**
     * Search for properties matching the criteria
     */
    fun searchProperties(location: String, size: Double, propertyType: PropertyType) {
        viewModelScope.launch {
            // Store search parameters
            currentLocation = location
            currentSize = size
            currentPropertyType = propertyType
            apiOffset = 0 // Reset API offset for new search
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null, listings = emptyList(), hasMoreResults = false, totalResults = 0, isFallback = false) }
            
            try {
                // Get the final result (skip Loading state)
                val result = propertyRepository.searchPropertyListings(location, size, propertyType, offset = 0, limit = 10)
                    .first { it !is NetworkResult.Loading }
                
                when (result) {
                    is NetworkResult.Success -> {
                        val searchResult = result.data
                        apiOffset = 50 // We fetched 50 results from API, so next offset is 50
                        totalAvailableResults = searchResult.total
                        
                        // hasMoreResults: true if we got a full page (10 results) and there might be more
                        // For fallback data, we don't have more results
                        val hasMore = !searchResult.isFallback && searchResult.listings.size >= 10
                        
                        _uiState.update {
                            it.copy(
                                listings = searchResult.listings,
                                isLoading = false,
                                isLoadingMore = false,
                                hasMoreResults = hasMore,
                                totalResults = totalAvailableResults,
                                errorMessage = null,
                                isFallback = searchResult.isFallback
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                errorMessage = result.message ?: "Failed to load properties",
                                isFallback = false
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
                        isLoadingMore = false,
                        errorMessage = "Error: ${e.message}",
                        isFallback = false
                    )
                }
            }
        }
    }
    
    /**
     * Load more properties using the same search criteria
     */
    fun loadMoreProperties() {
        viewModelScope.launch {
            if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreResults) {
                return@launch
            }
            
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            
            try {
                val result = propertyRepository.searchPropertyListings(
                    currentLocation, 
                    currentSize, 
                    currentPropertyType, 
                    offset = apiOffset, 
                    limit = 10
                ).first { it !is NetworkResult.Loading }
                
                when (result) {
                    is NetworkResult.Success -> {
                        val searchResult = result.data
                        val newListings = searchResult.listings
                        val updatedListings = _uiState.value.listings + newListings
                        apiOffset += 50 // Increment by 50 since we fetch 50 results at a time from API
                        
                        // hasMoreResults: true if we got a full page (10 results) in this load
                        // This indicates there might be more results available
                        val hasMore = newListings.size >= 10
                        
                        _uiState.update {
                            it.copy(
                                listings = updatedListings,
                                isLoadingMore = false,
                                hasMoreResults = hasMore,
                                errorMessage = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                errorMessage = result.message ?: "Failed to load more properties"
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
                        isLoadingMore = false,
                        errorMessage = "Error loading more: ${e.message}"
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
     * Retry the current search (useful when API was down and fallback was used)
     */
    fun retrySearch() {
        if (currentLocation.isNotEmpty() && currentSize > 0) {
            searchProperties(currentLocation, currentSize, currentPropertyType)
        }
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

