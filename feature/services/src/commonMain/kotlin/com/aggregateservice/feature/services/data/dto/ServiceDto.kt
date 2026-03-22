package com.aggregateservice.feature.services.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for provider service API response.
 *
 * @property id Unique service identifier
 * @property name Service name
 * @property description Service description
 * @property basePrice Base price in provider's currency
 * @property currency Currency code (default: ILS)
 * @property durationMinutes Duration in minutes
 * @property categoryId Category ID
 * @property isActive Whether service is available for booking
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 */
@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("base_price")
    val basePrice: Double,
    val currency: String = "ILS",
    @SerialName("duration_minutes")
    val durationMinutes: Int,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
)
