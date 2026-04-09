package com.aggregateservice.feature.services.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.model.UpdateServiceRequest
import com.aggregateservice.feature.services.domain.repository.ServicesRepository

/**
 * UseCase for updating an existing service.
 *
 * **Business Rules:**
 * - At least one field must be provided for update
 * - Name must be 3-100 characters if provided
 * - Base price must be non-negative if provided
 * - Duration must be 5-480 minutes if provided
 *
 * @property repository Services repository
 */
class UpdateServiceUseCase(
    private val repository: ServicesRepository,
) {
    /**
     * Updates an existing service.
     *
     * @param id The service ID to update
     * @param name New name (optional)
     * @param description New description (optional)
     * @param basePrice New base price (optional)
     * @param durationMinutes New duration (optional)
     * @param categoryId New category ID (optional)
     * @param isActive New active status (optional)
     * @return Result containing the updated service or an error
     */
    suspend operator fun invoke(
        id: String,
        name: String? = null,
        description: String? = null,
        basePrice: Double? = null,
        durationMinutes: Int? = null,
        categoryId: String? = null,
        isActive: Boolean? = null,
    ): Result<ProviderService> {
        // Validation: id
        if (id.isBlank()) {
            return Result.failure(
                AppError.FormValidation("id", ValidationRule.NotBlank),
            )
        }

        // Validation: at least one field to update
        if (name == null && description == null && basePrice == null &&
            durationMinutes == null && categoryId == null && isActive == null
        ) {
            return Result.failure(
                AppError.FormValidation("request", ValidationRule.Required),
            )
        }

        // Validation: name
        if (name != null) {
            if (name.isBlank()) {
                return Result.failure(
                    AppError.FormValidation("name", ValidationRule.NotBlank),
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
        }

        // Validation: basePrice
        if (basePrice != null && basePrice < 0) {
            return Result.failure(
                AppError.FormValidation("basePrice", ValidationRule.NonNegative),
            )
        }

        // Validation: durationMinutes
        if (durationMinutes != null) {
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
        }

        val request = UpdateServiceRequest(
            name = name?.trim(),
            description = description?.trim()?.takeIf { it.isNotBlank() },
            basePrice = basePrice,
            durationMinutes = durationMinutes,
            categoryId = categoryId,
            isActive = isActive,
        )

        return repository.updateService(id, request)
    }

    companion object {
        private const val MIN_NAME_LENGTH = 3
        private const val MAX_NAME_LENGTH = 100
        private const val MIN_DURATION = 5
        private const val MAX_DURATION = 480
    }
}
