package de.tum.hack.jb.interhyp.challenge.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for ThinkImmo API
 * POST https://thinkimmo-api.mgraetz.de/thinkimmo
 */
@Serializable
data class ThinkImmoRequestDto(
    @SerialName("active")
    val active: String, // API expects string "True"/"False", not boolean
    val type: PropertyTypeDto,
    val sortBy: SortDirection,
    val sortKey: SortKey,
    val from: Int,
    val size: Int,
    val geoSearches: GeoSearchDto? = null
)

@Serializable
enum class PropertyTypeDto {
    @SerialName("APARTMENTBUY")
    APARTMENTBUY,
    @SerialName("HOUSEBUY")
    HOUSEBUY,
    @SerialName("LANDBUY")
    LANDBUY,
    @SerialName("GARAGEBUY")
    GARAGEBUY,
    @SerialName("OFFICEBUY")
    OFFICEBUY
}

@Serializable
enum class SortDirection {
    @SerialName("desc")
    DESC,
    @SerialName("asc")
    ASC
}

@Serializable
enum class SortKey {
    @SerialName("buyingPrice")
    BUYING_PRICE,
    @SerialName("publishDate")
    PUBLISH_DATE,
    @SerialName("squareMeter")
    SQUARE_METER,
    @SerialName("rentPricePerSqm")
    RENT_PRICE_PER_SQM,
    @SerialName("grossReturn")
    GROSS_RETURN,
    @SerialName("constructionYear")
    CONSTRUCTION_YEAR,
    @SerialName("pricePerSqm")
    PRICE_PER_SQM
}

@Serializable
data class GeoSearchDto(
    val geoSearchQuery: String,
    val geoSearchType: GeoSearchType,
    val region: String? = null
)

@Serializable
enum class GeoSearchType {
    @SerialName("town")
    TOWN,
    @SerialName("city")
    CITY,
    @SerialName("state")
    STATE
}

/**
 * Response DTO for ThinkImmo API
 */
@Serializable
data class ThinkImmoResponseDto(
    val total: Int,
    val results: List<ResultItemDto>,
    val aggregations: AggregationsDto? = null
)

@Serializable
data class AggregationsDto(
    val averagePerWeek: AverageValueDto? = null
)

@Serializable
data class AverageValueDto(
    val value: Double
)

@Serializable
data class ImageDto(
    val id: String,
    val originalUrl: String,
    val title: String? = null,
    val floorPlan: Boolean = false
)

@Serializable
data class ResultItemDto(
    val id: String,
    val title: String? = null,
    val zip: String? = null,
    val buyingPrice: Double? = null,
    val rooms: Double? = null,
    val squareMeter: Double? = null,
    val comission: Double? = null,
    val platforms: List<PlatformDto>? = null,
    val rentPricePerSqm: Double? = null,
    val metaSpRentPricePerSqm: MetaDto? = null,
    val pricePerSqm: Double? = null,
    val spPricePerSqm: Double? = null,
    val metaSpPricePerSqm: MetaDto? = null,
    val rentPrice: Double? = null,
    val rentPriceCurrent: Double? = null,
    val rentPriceCurrentPerSqm: Double? = null,
    val address: AddressDto? = null,
    val energyEfficiencyClass: String? = null,
    val region: String? = null,
    val foreClosure: Boolean? = null,
    val locationFactor: LocationFactorDto? = null,
    val constructionYear: Int? = null,
    val yearOfRenovation: Int? = null,
    val heatingType: String? = null,
    val floor: Int? = null,
    val condition: String? = null,
    val balcony: Boolean? = null,
    val garden: Boolean? = null,
    val parking: Boolean? = null,
    val elevator: Boolean? = null,
    val cellar: Boolean? = null,
    val images: List<ImageDto>? = null
)

@Serializable
data class PlatformDto(
    val name: String,
    val id: String,
    val url: String,
    val creationDate: String? = null,
    val publishDate: String? = null,
    val active: Boolean
)

@Serializable
data class MetaDto(
    val model: String? = null,
    val standarderror: Double? = null,
    val score: Double? = null,
    val range: RangeDto? = null
)

@Serializable
data class RangeDto(
    val min: Double,
    val max: Double
)

@Serializable
data class AddressDto(
    val city: String? = null,
    val postcode: String? = null,
    val state: String? = null,
    val state_code: String? = null,
    val country: String? = null,
    val country_code: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val displayName: String? = null,
    val _normalized_city: String? = null,
    val _type: String? = null,
    val _category: String? = null,
    val continent: String? = null,
    val political_union: String? = null
)

@Serializable
data class LocationFactorDto(
    val population: Int? = null,
    val populationTrend: PopulationTrendDto? = null,
    val unemploymentRate: Double? = null,
    val gdpPerCapita: Double? = null,
    val crimeRate: Double? = null
)

@Serializable
data class PopulationTrendDto(
    val from: Int? = null,
    val to: Int? = null
)
