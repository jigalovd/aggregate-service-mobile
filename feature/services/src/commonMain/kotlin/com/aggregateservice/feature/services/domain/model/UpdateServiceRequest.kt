package com.aggregateservice.feature.services.domain.model

/**
 * Request model for updating an existing service.
 * All fields are optional - only provided fields will be updated.
 *
 * @property name New name for the service
 * @property description New description for the service
 * @property basePrice New base price (must be >= 0 if provided)
 * @property durationMinutes New duration in minutes (must be > 0 if provided)
 * @property categoryId New category ID
 * @property isActive Whether the service should be active
 */
data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val basePrice: Double? = null,
    val durationMinutes: Int? = null,
    val categoryId: String? = null,
    val isActive: Boolean? = null,
) {
    init {
        require(name?.isNotBlank() != false) { "Service name cannot be blank" }
        require(basePrice == null || basePrice >= 0) { "Base price must be non-negative" }
        require(durationMinutes == null || durationMinutes > 0) { "Duration must be positive" }
    }
}
