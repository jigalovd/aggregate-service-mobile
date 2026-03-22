package com.aggregateservice.feature.services.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for creating a new service.
 *
 * @property name Service name
 * @property description Service description
 * @property basePrice Base price
 * @property durationMinutes Duration in minutes
 * @property categoryId Category ID
 */
@Serializable
data class CreateServiceRequestDto(
    val name: String,
    val description: String? = null,
    @SerialName("base_price")
    val basePrice: Double,
    @SerialName("duration_minutes")
    val durationMinutes: Int,
    @SerialName("category_id")
    val categoryId: String,
)
