package com.aggregateservice.feature.services.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.repository.ServicesRepository

/**
 * UseCase for retrieving a specific service by ID.
 *
 * @property repository Services repository
 */
class GetServiceByIdUseCase(
    private val repository: ServicesRepository,
) {
    /**
     * Retrieves a service by its ID.
     *
     * @param id The service ID
     * @return Result containing the service or an error
     */
    suspend operator fun invoke(id: String): Result<ProviderService> {
        if (id.isBlank()) {
            return Result.failure(
                AppError.ValidationError(
                    field = "id",
                    message = "Service ID cannot be empty",
                ),
            )
        }

        return repository.getServiceById(id)
    }
}
