package com.aggregateservice.feature.services.domain.model

import kotlinx.datetime.Instant

/**
 * Domain entity representing a provider's service offering.
 *
 * **Note:** Domain models must NOT import Compose/Android dependencies.
 * Stability is ensured by data class immutability.
 *
 * @property id Unique identifier for the service
 * @property name Name of the service
 * @property description Optional description of the service
 * @property basePrice Base price in the provider's currency
 * @property durationMinutes Duration of the service in minutes
 * @property categoryId ID of the category this service belongs to
 * @property isActive Whether the service is currently available for booking
 * @property createdAt Timestamp when the service was created
 * @property updatedAt Timestamp when the service was last updated
 */
data class ProviderService(
    val id: String,
    val name: String,
    val description: String?,
    val basePrice: Double,
    val durationMinutes: Int,
    val categoryId: String,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val formattedPrice: String
        get() = String.format("%.2f", basePrice)

    val formattedDuration: String
        get() = when {
            durationMinutes < 60 -> "${durationMinutes}min"
            durationMinutes % 60 == 0 -> "${durationMinutes / 60}h"
            else -> "${durationMinutes / 60}h ${durationMinutes % 60}min"
        }
}
