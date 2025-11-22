package de.tum.hack.jb.interhyp.challenge.domain.model

import kotlinx.serialization.Serializable

/**
 * Property entity representing a real estate property that the user saves for.
 */
@Serializable
data class Property(
    val id: String,
    val location: String,
    val price: Double,
    val images: List<String> = emptyList(),
    val size: Double? = null, // in square meters
    val type: PropertyType = PropertyType.APARTMENT,
    val description: String? = null
)

/**
 * Property type enum
 */
@Serializable
enum class PropertyType {
    APARTMENT,
    HOUSE,
    CONDO,
    VILLA
}
