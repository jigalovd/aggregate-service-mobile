package com.aggregateservice.feature.services.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.repository.ServicesRepository

/**
 * UseCase for retrieving all services for the authenticated provider.
 *
 * @property repository Services repository
 */
class GetServicesUseCase(
    private val repository: ServicesRepository,
) {
    /**
     * Retrieves all services for the current provider.
     *
     * @return Result containing list of services or an error
     */
    suspend operator fun invoke(): Result<List<ProviderService>> {
        return repository.getServices()
    }
}
