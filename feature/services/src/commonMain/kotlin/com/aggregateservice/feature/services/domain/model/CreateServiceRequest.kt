package com.aggregateservice.feature.services.domain.model

/**
 * Request model for creating a new service.
 *
 * @property name Name of the service (required)
 * @property description Optional description of the service
 * @property basePrice Base price in the provider's currency (required, must be >= 0)
 * @property durationMinutes Duration of the service in minutes (required, must be > 0)
 * @property categoryId ID of the category this service belongs to (required)
 */
data class CreateServiceRequest(
    val name: String,
    val description: String?,
    val basePrice: Double,
    val durationMinutes: Int,
    val categoryId: String,
) {
    init {
        require(name.isNotBlank()) { "Service name cannot be blank" }
        require(basePrice >= 0) { "Base price must be non-negative" }
        require(durationMinutes > 0) { "Duration must be positive" }
        require(categoryId.isNotBlank()) { "Category ID cannot be blank" }
    }
}
