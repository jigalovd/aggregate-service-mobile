package com.aggregateservice.feature.catalog.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для запроса поиска провайдеров.
 *
 * Использует имена полей согласно backend API:
 * - lat/lon вместо latitude/longitude
 * - radius_km вместо radiusKm
 * - sort_by вместо sortBy
 * - category_id (UUID) вместо categoryIds (list)
 *
 * @property lat Latitude
 * @property lon Longitude
 * @property radiusKm Search radius in km
 * @property categoryId Category ID filter (optional)
 * @property serviceId Service ID filter (optional)
 * @property priceMin Min price in cents (optional)
 * @property priceMax Max price in cents (optional)
 * @property sortBy Sort field: "distance" or "rating"
 * @property limit Results limit
 * @property offset Results offset
 */
@Serializable
data class ProviderSearchRequestDto(
    val lat: Double,
    val lon: Double,
    @SerialName("radius_km")
    val radiusKm: Double = 30.0,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("service_id")
    val serviceId: String? = null,
    @SerialName("price_min")
    val priceMin: Int? = null,
    @SerialName("price_max")
    val priceMax: Int? = null,
    @SerialName("sort_by")
    val sortBy: String = "distance",
    val limit: Int = 20,
    val offset: Int = 0,
)
