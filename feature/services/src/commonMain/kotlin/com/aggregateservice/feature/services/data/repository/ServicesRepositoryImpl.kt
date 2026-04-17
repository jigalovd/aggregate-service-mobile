package com.aggregateservice.feature.services.data.repository

import com.aggregateservice.core.api.models.ProviderServiceResponse
import com.aggregateservice.feature.services.data.api.ServicesApiService
import com.aggregateservice.feature.services.data.mapper.ServiceMapper
import com.aggregateservice.feature.services.domain.model.CreateServiceRequest
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.model.UpdateServiceRequest
import com.aggregateservice.feature.services.domain.repository.ServicesRepository

/**
 * Implementation of ServicesRepository.
 *
 * **Architecture:**
 * - Data layer implements Domain layer interface
 * - Uses ServicesApiService for network requests
 * - Uses ServiceMapper for DTO -> Domain conversion
 *
 * @property apiService API service for provider services
 */
class ServicesRepositoryImpl(
    private val apiService: ServicesApiService,
) : ServicesRepository {

    override suspend fun getServices(): Result<List<ProviderService>> {
        return apiService.getServices().fold(
            onSuccess = { dtos: List<ProviderServiceResponse> -> Result.success(ServiceMapper.toDomain(dtos)) },
            onFailure = { error: Throwable -> Result.failure(error) },
        )
    }

    override suspend fun getServiceById(id: String): Result<ProviderService> {
        return apiService.getServiceById(id).fold(
            onSuccess = { dto: ProviderServiceResponse -> Result.success(ServiceMapper.toDomain(dto)) },
            onFailure = { error: Throwable -> Result.failure(error) },
        )
    }

    override suspend fun createService(request: CreateServiceRequest): Result<ProviderService> {
        val requestDto = ServiceMapper.toDto(request)

        return apiService.createService(requestDto).fold(
            onSuccess = { dto: ProviderServiceResponse -> Result.success(ServiceMapper.toDomain(dto)) },
            onFailure = { error: Throwable -> Result.failure(error) },
        )
    }

    override suspend fun updateService(
        id: String,
        request: UpdateServiceRequest,
    ): Result<ProviderService> {
        val requestDto = ServiceMapper.toDto(request)

        return apiService.updateService(id, requestDto).fold(
            onSuccess = { dto: ProviderServiceResponse -> Result.success(ServiceMapper.toDomain(dto)) },
            onFailure = { error: Throwable -> Result.failure(error) },
        )
    }

    override suspend fun deleteService(id: String): Result<Unit> {
        return apiService.deleteService(id)
    }
}
