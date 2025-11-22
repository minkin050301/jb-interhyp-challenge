package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.network.dto.*
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
        private const val THINKIMMO_API_URL = "https://thinkimmo-api.mgraetz.de/thinkimmo"
        
        // Default fallback values per location (in EUR per sqm)
        private val DEFAULT_PRICES = mapOf(
            "Munich" to 9000.0,
            "Muenchen" to 9000.0,
            "Berlin" to 6000.0,
            "Hamburg" to 5500.0,
            "Frankfurt" to 6500.0,
            "Cologne" to 4500.0,
            "Koeln" to 4500.0,
            "Stuttgart" to 5000.0,
            "default" to 5000.0
        )
    }
    
    override suspend fun getPropertyListings(location: String): Flow<NetworkResult<List<PropertyListing>>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            // Normalize location for German umlauts (ä→ae, ö→oe, ü→ue, ß→ss)
            val normalizedLocation = normalizeGermanUmlauts(location)
            
            // Build request for ThinkImmo API
            val request = ThinkImmoRequestDto(
                active = true,
                type = PropertyTypeDto.APARTMENTBUY,
                sortBy = SortDirection.DESC,
                sortKey = SortKey.PRICE_PER_SQM,
                from = 0,
                size = 20,
                geoSearches = GeoSearchDto(
                    geoSearchQuery = normalizedLocation,
                    geoSearchType = GeoSearchType.CITY,
                    region = null
                )
            )
            
            // POST request to ThinkImmo API
            val response = httpClient.post(THINKIMMO_API_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val apiResponse: ThinkImmoResponseDto = response.body()
                val listings = mapApiResponseToListings(apiResponse, location)
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
            // Try to get real-time data from API first
            val normalizedLocation = normalizeGermanUmlauts(location)
            
            val request = ThinkImmoRequestDto(
                active = true,
                type = PropertyTypeDto.APARTMENTBUY,
                sortBy = SortDirection.DESC,
                sortKey = SortKey.PRICE_PER_SQM,
                from = 0,
                size = 10,
                geoSearches = GeoSearchDto(
                    geoSearchQuery = normalizedLocation,
                    geoSearchType = GeoSearchType.CITY,
                    region = null
                )
            )
            
            val response = httpClient.post(THINKIMMO_API_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            val pricePerSqm = if (response.status.isSuccess()) {
                val apiResponse: ThinkImmoResponseDto = response.body()
                val validPrices = apiResponse.results.mapNotNull { it.pricePerSqm }
                if (validPrices.isNotEmpty()) {
                    validPrices.average()
                } else {
                    DEFAULT_PRICES[location] ?: DEFAULT_PRICES["default"]!!
                }
            } else {
                DEFAULT_PRICES[location] ?: DEFAULT_PRICES["default"]!!
            }
            
            val totalPrice = pricePerSqm * size
            emit(NetworkResult.Success(totalPrice))
        } catch (e: Exception) {
            // Fallback to default prices
            val pricePerSqm = DEFAULT_PRICES[location] ?: DEFAULT_PRICES["default"]!!
            val totalPrice = pricePerSqm * size
            emit(NetworkResult.Success(totalPrice))
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
    
    /**
     * Normalize German umlauts as per API documentation
     * ä→ae, ö→oe, ü→ue, ß→ss
     */
    private fun normalizeGermanUmlauts(text: String): String {
        return text
            .replace("ä", "ae")
            .replace("Ä", "Ae")
            .replace("ö", "oe")
            .replace("Ö", "Oe")
            .replace("ü", "ue")
            .replace("Ü", "Ue")
            .replace("ß", "ss")
    }
    
    /**
     * Map ThinkImmo API response to domain PropertyListing models
     */
    private fun mapApiResponseToListings(
        response: ThinkImmoResponseDto,
        location: String
    ): List<PropertyListing> {
        if (response.results.isEmpty()) {
            return getDefaultPropertyListing(location)
        }
        
        // Calculate average price per sqm from results
        val validPrices = response.results.mapNotNull { it.pricePerSqm }
        val averagePricePerSqm = if (validPrices.isNotEmpty()) {
            validPrices.average()
        } else {
            DEFAULT_PRICES[location] ?: DEFAULT_PRICES["default"]!!
        }
        
        val minPrice = validPrices.minOrNull()
        val maxPrice = validPrices.maxOrNull()
        
        return listOf(
            PropertyListing(
                id = "thinkimmo-$location",
                location = location,
                averagePrice = averagePricePerSqm * 80, // Assuming 80 sqm average
                pricePerSqm = averagePricePerSqm,
                minPrice = minPrice?.let { it * 80 },
                maxPrice = maxPrice?.let { it * 80 },
                availableProperties = response.total,
                marketTrend = MarketTrend.STABLE
            )
        )
    }
}
