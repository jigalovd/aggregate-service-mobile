package com.aggregateservice.feature.services.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.services.domain.repository.ServicesRepository

/**
 * UseCase for deleting a service.
 *
 * @property repository Services repository
 */
class DeleteServiceUseCase(
    private val repository: ServicesRepository,
) {
    /**
     * Deletes a service by its ID.
     *
     * @param id The service ID to delete
     * @return Result indicating success or an error
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(
                AppError.ValidationError(
                    field = "id",
                    message = "Service ID cannot be empty",
                ),
            )
        }

        return repository.deleteService(id)
    }
}
