package de.tum.hack.jb.interhyp.challenge.data.repository

import de.tum.hack.jb.interhyp.challenge.data.network.NetworkResult
import de.tum.hack.jb.interhyp.challenge.data.network.dto.*
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyListing
import de.tum.hack.jb.interhyp.challenge.domain.model.MarketTrend
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyListingDto
import de.tum.hack.jb.interhyp.challenge.domain.model.PropertyType
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
    
    /**
     * Search property listings with location, size, and property type.
     * Filters results by size with ±20% tolerance.
     */
    suspend fun searchPropertyListings(
        location: String,
        size: Double,
        propertyType: PropertyType
    ): Flow<NetworkResult<List<PropertyListingDto>>>
}

/**
 * Implementation of PropertyRepository with ThinkImmo API integration.
 */
class PropertyRepositoryImpl(
    private val httpClient: HttpClient
) : PropertyRepository {
    
    companion object {
        private const val THINKIMMO_API_URL = "https://thinkimmo-api.mgraetz.de/thinkimmo"
        
        // Default fallback values per location for apartments (in EUR per sqm)
        // Based on average market prices in German cities (2024)
        private val DEFAULT_APARTMENT_PRICES = mapOf(
            "Munich" to 9500.0,
            "Muenchen" to 9500.0,
            "Berlin" to 6500.0,
            "Hamburg" to 6000.0,
            "Frankfurt" to 7000.0,
            "Cologne" to 5000.0,
            "Koeln" to 5000.0,
            "Stuttgart" to 5500.0,
            "default" to 5000.0
        )
        
        // Default fallback values per location for houses (in EUR per sqm)
        // Houses are typically 15-20% more expensive per sqm than apartments
        private val DEFAULT_HOUSE_PRICES = mapOf(
            "Munich" to 11000.0,
            "Muenchen" to 11000.0,
            "Berlin" to 7500.0,
            "Hamburg" to 7000.0,
            "Frankfurt" to 8000.0,
            "Cologne" to 5800.0,
            "Koeln" to 5800.0,
            "Stuttgart" to 6500.0,
            "default" to 5800.0
        )
        
        // Legacy default prices (for backward compatibility)
        private val DEFAULT_PRICES = DEFAULT_APARTMENT_PRICES
    }
    
    /**
     * Get price per sqm for a location and property type
     */
    private fun getPricePerSqm(location: String, propertyType: PropertyType): Double {
        val priceMap = when (propertyType) {
            PropertyType.HOUSE -> DEFAULT_HOUSE_PRICES
            else -> DEFAULT_APARTMENT_PRICES
        }
        return priceMap[location] ?: priceMap["default"]!!
    }
    
    override suspend fun getPropertyListings(location: String): Flow<NetworkResult<List<PropertyListing>>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            // Normalize location for German umlauts (ä→ae, ö→oe, ü→ue, ß→ss)
            val normalizedLocation = normalizeGermanUmlauts(location)
            
            // Build request for ThinkImmo API
            val request = ThinkImmoRequestDto(
                active = "True", // API expects string, not boolean
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
                active = "True", // API expects string, not boolean
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
                    // Fallback to apartment prices (default)
                    getPricePerSqm(location, PropertyType.APARTMENT)
                }
            } else {
                // Fallback to apartment prices (default)
                getPricePerSqm(location, PropertyType.APARTMENT)
            }
            
            val totalPrice = pricePerSqm * size
            emit(NetworkResult.Success(totalPrice))
        } catch (e: Exception) {
            // Fallback to default prices (apartment prices)
            val pricePerSqm = getPricePerSqm(location, PropertyType.APARTMENT)
            val totalPrice = pricePerSqm * size
            emit(NetworkResult.Success(totalPrice))
        }
    }
    
    /**
     * Generate default property listing for fallback
     */
    private fun getDefaultPropertyListing(location: String): List<PropertyListing> {
        val pricePerSqm = getPricePerSqm(location, PropertyType.APARTMENT)
        
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
     * Convert English city names to German names with umlauts for API calls
     * The ThinkImmo API requires exact German city names
     */
    private fun convertToGermanCityName(city: String): String {
        return when (city.lowercase()) {
            "munich" -> "München"
            "cologne" -> "Köln"
            "nuremberg" -> "Nürnberg"
            "hanover" -> "Hannover"
            "dusseldorf", "duesseldorf" -> "Düsseldorf"
            else -> city // Keep original if no mapping needed
        }
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
            getPricePerSqm(location, PropertyType.APARTMENT)
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
    
    /**
     * Map PropertyType enum to PropertyTypeDto
     */
    private fun mapPropertyTypeToDto(propertyType: PropertyType): PropertyTypeDto {
        return when (propertyType) {
            PropertyType.HOUSE -> PropertyTypeDto.HOUSEBUY
            PropertyType.APARTMENT -> PropertyTypeDto.APARTMENTBUY
            else -> PropertyTypeDto.APARTMENTBUY // Default fallback
        }
    }
    
    /**
     * Map city name to German state name (region)
     * Returns full German state name as the API expects (e.g., "Bayern" not "BY")
     */
    private fun getRegionForCity(city: String): String? {
        val cityLower = city.lowercase()
        return when {
            cityLower.contains("munich") || cityLower.contains("muenchen") || 
            cityLower.contains("münchen") -> "Bayern"
            cityLower.contains("berlin") -> "Berlin"
            cityLower.contains("hamburg") -> "Hamburg"
            cityLower.contains("frankfurt") -> "Hessen"
            cityLower.contains("cologne") || cityLower.contains("koeln") || 
            cityLower.contains("köln") -> "Nordrhein-Westfalen"
            cityLower.contains("stuttgart") -> "Baden-Württemberg"
            cityLower.contains("dresden") -> "Sachsen"
            cityLower.contains("leipzig") -> "Sachsen"
            cityLower.contains("düsseldorf") || cityLower.contains("duesseldorf") -> "Nordrhein-Westfalen"
            cityLower.contains("essen") -> "Nordrhein-Westfalen"
            cityLower.contains("dortmund") -> "Nordrhein-Westfalen"
            cityLower.contains("hannover") || cityLower.contains("hanover") -> "Niedersachsen"
            cityLower.contains("bremen") -> "Bremen"
            cityLower.contains("nuremberg") || cityLower.contains("nürnberg") || 
            cityLower.contains("nuernberg") -> "Bayern"
            else -> null // Unknown city
        }
    }
    
    /**
     * Generate fallback property listings when API fails
     * Creates synthetic listings based on average prices per sqm for the city and property type
     */
    private fun generateFallbackListings(
        location: String,
        desiredSize: Double,
        propertyType: PropertyType
    ): List<PropertyListingDto> {
        val pricePerSqm = getPricePerSqm(location, propertyType)
        
        // Generate 5-8 synthetic listings with variations in size and price
        val listings = mutableListOf<PropertyListingDto>()
        val propertyTypeName = if (propertyType == PropertyType.HOUSE) "House" else "Apartment"
        
        // Generate listings with size variations (±20%)
        val sizeVariations = listOf(
            desiredSize * 0.85,
            desiredSize * 0.90,
            desiredSize * 0.95,
            desiredSize,
            desiredSize * 1.05,
            desiredSize * 1.10,
            desiredSize * 1.15
        )
        
        sizeVariations.forEachIndexed { index, listingSize ->
            // Add some price variation (±10%)
            val priceVariation = 0.9 + (index * 0.033) // Range from 0.9 to 1.1
            val listingPricePerSqm = pricePerSqm * priceVariation
            val totalPrice = listingPricePerSqm * listingSize
            
            // Estimate rooms based on size (roughly 1 room per 25-30 sqm)
            val estimatedRooms = (listingSize / 27.5).toInt().coerceIn(1, 10)
            
            listings.add(
                PropertyListingDto(
                    id = "fallback-${location.lowercase()}-${propertyType.name.lowercase()}-$index",
                    title = "$propertyTypeName in $location",
                    buyingPrice = totalPrice,
                    squareMeter = listingSize,
                    rooms = estimatedRooms.toDouble(),
                    address = de.tum.hack.jb.interhyp.challenge.data.network.dto.AddressDto(
                        city = location,
                        displayName = location
                    ),
                    images = null,
                    zip = null,
                    pricePerSqm = listingPricePerSqm
                )
            )
        }
        
        return listings
    }
    
    override suspend fun searchPropertyListings(
        location: String,
        size: Double,
        propertyType: PropertyType
    ): Flow<NetworkResult<List<PropertyListingDto>>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            // Normalize location for German umlauts
            val normalizedLocation = normalizeGermanUmlauts(location)
            
            // Map property type
            val propertyTypeDto = mapPropertyTypeToDto(propertyType)
            
            // Get region code for the city
            val region = getRegionForCity(location)
            
            // Build request for ThinkImmo API with updated parameters
            // Convert English city names to German names with umlauts (e.g., "Munich" -> "München")
            // API expects "active" as string "True"/"False", not boolean
            // IMPORTANT: All fields must be explicitly set (no defaults) to ensure they're serialized
            val germanCityName = convertToGermanCityName(location)
            
            val request = ThinkImmoRequestDto(
                active = "True", // API expects string, not boolean
                type = propertyTypeDto,
                sortBy = SortDirection.DESC,
                sortKey = SortKey.PUBLISH_DATE, // Sort by publish date
                from = 0,
                size = 50, // Get more results to filter from
                geoSearches = GeoSearchDto(
                    geoSearchQuery = germanCityName, // Use German city name with umlauts (e.g., "München")
                    geoSearchType = GeoSearchType.TOWN, // Use "town" instead of "city"
                    region = region // Include full German state name (e.g., "Bayern")
                )
            )
            
            // Debug: Log request details
            println("DEBUG: PropertyRepository - Request: active=${request.active}, type=${request.type}, location=$location, region=$region")
            println("DEBUG: PropertyRepository - geoSearchQuery=${request.geoSearches?.geoSearchQuery}, geoSearchType=${request.geoSearches?.geoSearchType}")
            
            // Serialize request to JSON for debugging (using same config as Ktor)
            try {
                val json = kotlinx.serialization.json.Json { 
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true // IMPORTANT: Include default values
                }
                val requestJson = json.encodeToString(ThinkImmoRequestDto.serializer(), request)
                println("DEBUG: PropertyRepository - Request JSON: $requestJson")
            } catch (e: Exception) {
                println("DEBUG: PropertyRepository - Could not serialize request: ${e.message}")
            }
            
            // POST request to ThinkImmo API
            val response = httpClient.post(THINKIMMO_API_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("DEBUG: PropertyRepository - Response status: ${response.status.value}")
            
            // Handle both 200 OK and 201 Created as success
            if (response.status.value in 200..299) {
                try {
                    // Try to parse as JSON
                    val apiResponse: ThinkImmoResponseDto = response.body()
                    
                    println("DEBUG: PropertyRepository - Parsed response - total: ${apiResponse.total}, results count: ${apiResponse.results.size}")
                    
                    // Check if we have results
                    if (apiResponse.results.isEmpty()) {
                        // If API returns empty results, return empty list
                        println("DEBUG: PropertyRepository - API returned empty results (total: ${apiResponse.total})")
                        emit(NetworkResult.Success(emptyList()))
                    } else {
                        println("DEBUG: PropertyRepository - API returned ${apiResponse.results.size} results, processing...")
                        // Map results to PropertyListingDto and filter by size (±20% tolerance)
                        val sizeMin = size * 0.8
                        val sizeMax = size * 1.2
                        
                        val listings = apiResponse.results.mapNotNull { result ->
                            try {
                                // Filter by size if squareMeter is available
                                if (result.squareMeter != null) {
                                    if (result.squareMeter < sizeMin || result.squareMeter > sizeMax) {
                                        // Size doesn't match, skip this result
                                        return@mapNotNull null
                                    }
                                }
                                
                                PropertyListingDto(
                                    id = result.id,
                                    title = result.title,
                                    buyingPrice = result.buyingPrice,
                                    squareMeter = result.squareMeter,
                                    rooms = result.rooms,
                                    address = result.address,
                                    images = result.images,
                                    zip = result.zip,
                                    pricePerSqm = result.pricePerSqm
                                )
                            } catch (e: Exception) {
                                println("DEBUG: PropertyRepository - Error mapping result: ${e.message}")
                                null
                            }
                        }.take(10) // Limit to 10 listings
                        
                        println("DEBUG: PropertyRepository - Filtered to ${listings.size} listings (size filter: ${sizeMin.toInt()}-${sizeMax.toInt()} m²)")
                        listings.forEachIndexed { index, listing ->
                            println("DEBUG: Listing $index - ID: ${listing.id}, Title: ${listing.title}, Price: ${listing.buyingPrice}, Size: ${listing.squareMeter}")
                        }
                        emit(NetworkResult.Success(listings))
                    }
                } catch (e: Exception) {
                    // CancellationException (including AbortFlowException) is expected when using .first() - don't treat as error
                    if (e is kotlin.coroutines.cancellation.CancellationException) {
                        // This is normal - Flow was aborted after first result
                        throw e // Re-throw cancellation exceptions
                    }
                    // If parsing fails, return error
                    println("DEBUG: PropertyRepository - Exception parsing response: ${e.message}")
                    println("DEBUG: PropertyRepository - Exception type: ${e::class.simpleName}")
                    e.printStackTrace()
                    emit(NetworkResult.Error("Failed to parse API response: ${e.message}"))
                }
            } else {
                // Return error for non-success status
                emit(NetworkResult.Error("API returned status ${response.status.value}"))
            }
        } catch (e: Exception) {
            // CancellationException (including AbortFlowException) is expected when using .first() - don't treat as error
            if (e is kotlin.coroutines.cancellation.CancellationException) {
                // This is normal - Flow was aborted after first result
                throw e // Re-throw cancellation exceptions
            }
            // Return error instead of fallback
            println("DEBUG: PropertyRepository - Exception in searchPropertyListings: ${e.message}")
            e.printStackTrace()
            emit(NetworkResult.Error("Failed to fetch properties: ${e.message}"))
        }
    }
}
