package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * PropertyListing data class for API responses from ThinkImmo.
 * Represents property listings with market data.
 */
@Serializable
data class PropertyListing(
    val id: String,
    val location: String,
    val averagePrice: Double,
    val pricePerSqm: Double,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val availableProperties: Int = 0,
    val marketTrend: MarketTrend = MarketTrend.STABLE
) {
    /**
     * Calculate estimated property price based on desired size
     */
    fun calculateEstimatedPrice(desiredSize: Double): Double {
        return pricePerSqm * desiredSize
    }
    
    /**
     * Convert PropertyListing to Property entity
     */
    fun toProperty(desiredSize: Double): Property {
        return Property(
            id = id,
            location = location,
            price = calculateEstimatedPrice(desiredSize),
            size = desiredSize
        )
    }
}

/**
 * Market trend enum for property listings
 */
@Serializable
enum class MarketTrend {
    RISING,
    STABLE,
    FALLING
}
