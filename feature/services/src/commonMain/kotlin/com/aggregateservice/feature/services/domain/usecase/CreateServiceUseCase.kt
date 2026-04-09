package com.aggregateservice.feature.services.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.services.domain.model.CreateServiceRequest
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.repository.ServicesRepository

/**
 * UseCase for creating a new service.
 *
 * **Business Rules:**
 * - Name must be 3-100 characters
 * - Base price must be non-negative
 * - Duration must be 5-480 minutes (8 hours max)
 * - Category ID is required
 *
 * @property repository Services repository
 */
class CreateServiceUseCase(
    private val repository: ServicesRepository,
) {
    /**
     * Creates a new service.
     *
     * @param name Service name (3-100 characters)
     * @param description Optional description
     * @param basePrice Base price (must be >= 0)
     * @param durationMinutes Duration in minutes (5-480)
     * @param categoryId Category ID
     * @return Result containing the created service or an error
     */
    suspend operator fun invoke(
        name: String,
        description: String?,
        basePrice: Double,
        durationMinutes: Int,
        categoryId: String,
    ): Result<ProviderService> {
        // Validation: name
        if (name.isBlank()) {
            return Result.failure(
                AppError.FormValidation("name", ValidationRule.Required),
            )
        }
        if (name.length < MIN_NAME_LENGTH) {
            return Result.failure(
                AppError.FormValidation("name", ValidationRule.TooShort, mapOf("min" to MIN_NAME_LENGTH)),
            )
        }
        if (name.length > MAX_NAME_LENGTH) {
            return Result.failure(
                AppError.FormValidation("name", ValidationRule.TooLong, mapOf("max" to MAX_NAME_LENGTH)),
            )
        }

        // Validation: basePrice
        if (basePrice < 0) {
            return Result.failure(
                AppError.FormValidation("basePrice", ValidationRule.NonNegative),
            )
        }

        // Validation: durationMinutes
        if (durationMinutes < MIN_DURATION) {
            return Result.failure(
                AppError.FormValidation("durationMinutes", ValidationRule.TooLow, mapOf("min" to MIN_DURATION)),
            )
        }
        if (durationMinutes > MAX_DURATION) {
            return Result.failure(
                AppError.FormValidation("durationMinutes", ValidationRule.TooHigh, mapOf("max" to MAX_DURATION)),
            )
        }

        // Validation: categoryId
        if (categoryId.isBlank()) {
            return Result.failure(
                AppError.FormValidation("categoryId", ValidationRule.Required),
            )
        }

        val request = CreateServiceRequest(
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotBlank() },
            basePrice = basePrice,
            durationMinutes = durationMinutes,
            categoryId = categoryId,
        )

        return repository.createService(request)
    }

    companion object {
        private const val MIN_NAME_LENGTH = 3
        private const val MAX_NAME_LENGTH = 100
        private const val MIN_DURATION = 5
        private const val MAX_DURATION = 480 // 8 hours
    }
}
