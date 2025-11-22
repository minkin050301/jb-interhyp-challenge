package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyListing
import de.tum.hack.jb.interhyp.challenge.domain.model.MarketTrend
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository interface for property-related operations.
 */
interface PropertyRepository {
    /**
     * Fetch property listings from ThinkImmo API.
     * Falls back to default values on error.
     */
    suspend fun getPropertyListings(location: String): Flow<NetworkResult<List<PropertyListing>>>
    
    /**
     * Get average property price for a specific location.
     */
    suspend fun getAveragePrice(location: String, size: Double): Flow<NetworkResult<Double>>
}

/**
 * Implementation of PropertyRepository with ThinkImmo API integration.
 */
class PropertyRepositoryImpl(
    private val httpClient: HttpClient
) : PropertyRepository {
    
    companion object {
        private const val THINKIMMO_BASE_URL = "https://api.thinkimmo.com/v1"
        
        // Default fallback values per location (in EUR per sqm)
        private val DEFAULT_PRICES = mapOf(
            "Munich" to 9000.0,
            "Berlin" to 6000.0,
            "Hamburg" to 5500.0,
            "Frankfurt" to 6500.0,
            "Cologne" to 4500.0,
            "Stuttgart" to 5000.0,
            "default" to 5000.0
        )
    }
    
    override suspend fun getPropertyListings(location: String): Flow<NetworkResult<List<PropertyListing>>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            // Attempt to fetch from ThinkImmo API
            val response = httpClient.get("$THINKIMMO_BASE_URL/properties") {
                parameter("location", location)
                contentType(ContentType.Application.Json)
            }
            
            if (response.status.isSuccess()) {
                val listings: List<PropertyListing> = response.body()
                emit(NetworkResult.Success(listings))
            } else {
                // Fallback to default values
                emit(NetworkResult.Success(getDefaultPropertyListing(location)))
            }
        } catch (e: Exception) {
            // Fallback to default values on error
            emit(NetworkResult.Success(getDefaultPropertyListing(location)))
        }
    }
    
    override suspend fun getAveragePrice(location: String, size: Double): Flow<NetworkResult<Double>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            val pricePerSqm = DEFAULT_PRICES[location] ?: DEFAULT_PRICES["default"]!!
            val totalPrice = pricePerSqm * size
            emit(NetworkResult.Success(totalPrice))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to calculate price: ${e.message}", e))
        }
    }
    
    /**
     * Generate default property listing for fallback
     */
    private fun getDefaultPropertyListing(location: String): List<PropertyListing> {
        val pricePerSqm = DEFAULT_PRICES[location] ?: DEFAULT_PRICES["default"]!!
        
        return listOf(
            PropertyListing(
                id = "default-$location",
                location = location,
                averagePrice = pricePerSqm * 80, // Assuming 80 sqm average
                pricePerSqm = pricePerSqm,
                minPrice = pricePerSqm * 50,
                maxPrice = pricePerSqm * 150,
                availableProperties = 0,
                marketTrend = MarketTrend.STABLE
            )
        )
    }
}
