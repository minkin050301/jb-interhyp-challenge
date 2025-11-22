package de.tum.hack.jb.interhyp.challenge.domain.model

import de.tum.hack.jb.interhyp.challenge.data.network.dto.AddressDto
import de.tum.hack.jb.interhyp.challenge.data.network.dto.ImageDto
import kotlinx.serialization.Serializable

/**
 * Domain model for displaying property listings from ThinkImmo API.
 * Maps from ResultItemDto to a more user-friendly format.
 */
@Serializable
data class PropertyListingDto(
    val id: String,
    val title: String? = null,
    val buyingPrice: Double? = null,
    val squareMeter: Double? = null,
    val rooms: Double? = null,
    val address: AddressDto? = null,
    val images: List<ImageDto>? = null,
    val zip: String? = null,
    val pricePerSqm: Double? = null
) {
    /**
     * Get the first non-floor-plan image URL, or null if none available
     */
    fun getMainImageUrl(): String? {
        return images?.firstOrNull { !it.floorPlan }?.originalUrl
    }
    
    /**
     * Format buying price as currency string
     */
    fun getFormattedPrice(): String {
        return buyingPrice?.let {
            "${it.toInt()} €"
        } ?: "Price on request"
    }
    
    /**
     * Format size with unit
     */
    fun getFormattedSize(): String {
        return squareMeter?.let {
            "${it.toInt()} m²"
        } ?: "Size not specified"
    }
    
    /**
     * Format rooms count
     */
    fun getFormattedRooms(): String {
        return rooms?.let {
            "${it.toInt()} rooms"
        } ?: "Rooms not specified"
    }
    
    /**
     * Get location display string
     */
    fun getLocationDisplay(): String {
        return address?.displayName ?: address?.city ?: zip ?: "Location not specified"
    }
    
    /**
     * Get short description for display
     */
    fun getShortDescription(): String {
        val parts = mutableListOf<String>()
        title?.let { parts.add(it) }
        squareMeter?.let { parts.add("${it.toInt()} m²") }
        rooms?.let { parts.add("${it.toInt()} rooms") }
        return parts.joinToString(" • ")
    }
}

