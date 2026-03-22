package com.aggregateservice.feature.services.domain.repository

import com.aggregateservice.feature.services.domain.model.CreateServiceRequest
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.model.UpdateServiceRequest

/**
 * Repository interface for provider services management.
 * Provides CRUD operations for provider's services.
 */
interface ServicesRepository {
    /**
     * Retrieves all services for the authenticated provider.
     *
     * @return Result containing list of services or an error
     */
    suspend fun getServices(): Result<List<ProviderService>>

    /**
     * Retrieves a specific service by ID.
     *
     * @param id The service ID
     * @return Result containing the service or an error
     */
    suspend fun getServiceById(id: String): Result<ProviderService>

    /**
     * Creates a new service.
     *
     * @param request The creation request
     * @return Result containing the created service or an error
     */
    suspend fun createService(request: CreateServiceRequest): Result<ProviderService>

    /**
     * Updates an existing service.
     *
     * @param id The service ID to update
     * @param request The update request
     * @return Result containing the updated service or an error
     */
    suspend fun updateService(id: String, request: UpdateServiceRequest): Result<ProviderService>

    /**
     * Deletes a service.
     *
     * @param id The service ID to delete
     * @return Result indicating success or an error
     */
    suspend fun deleteService(id: String): Result<Unit>
}
