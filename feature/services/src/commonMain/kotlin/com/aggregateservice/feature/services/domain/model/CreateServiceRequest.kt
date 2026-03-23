package com.aggregateservice.feature.services.domain.model

/**
 * Request model for creating a new service.
 *
 * **Business Rules (US-4.1):**
 * - name: 3-100 символов
 * - basePrice: >= 0
 * - durationMinutes: 5-480 минут (5 минут минимум, 8 часов максимум)
 * - categoryId: обязателен
 *
 * @property name Name of the service (required, 3-100 chars)
 * @property description Optional description of the service
 * @property basePrice Base price in the provider's currency (required, must be >= 0)
 * @property durationMinutes Duration of the service in minutes (required, 5-480 min)
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
        // US-4.1: name 3-100 символов
        require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            "Service name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
        }
        require(basePrice >= 0) { "Base price must be non-negative" }
        // US-4.1: duration 5-480 минут
        require(durationMinutes in MIN_DURATION..MAX_DURATION) {
            "Duration must be between $MIN_DURATION and $MAX_DURATION minutes"
        }
        require(categoryId.isNotBlank()) { "Category ID cannot be blank" }
    }

    companion object {
        /**
         * Минимальная длина названия услуги (US-4.1).
         */
        const val MIN_NAME_LENGTH = 3

        /**
         * Максимальная длина названия услуги (US-4.1).
         */
        const val MAX_NAME_LENGTH = 100

        /**
         * Минимальная длительность услуги в минутах (US-4.1).
         */
        const val MIN_DURATION = 5

        /**
         * Максимальная длительность услуги в минутах (US-4.1).
         */
        const val MAX_DURATION = 480
    }
}
