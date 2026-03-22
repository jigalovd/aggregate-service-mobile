package com.aggregateservice.feature.services.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for updating an existing service.
 * All fields are optional - only provided fields will be updated.
 *
 * @property name New service name
 * @property description New description
 * @property basePrice New base price
 * @property durationMinutes New duration
 * @property categoryId New category ID
 * @property isActive New active status
 */
@Serializable
data class UpdateServiceRequestDto(
    val name: String? = null,
    val description: String? = null,
    @SerialName("base_price")
    val basePrice: Double? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null,
)
