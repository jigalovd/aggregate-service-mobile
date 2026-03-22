package com.aggregateservice.feature.services.data.mapper

import com.aggregateservice.feature.services.data.dto.CreateServiceRequestDto
import com.aggregateservice.feature.services.data.dto.ServiceDto
import com.aggregateservice.feature.services.data.dto.UpdateServiceRequestDto
import com.aggregateservice.feature.services.domain.model.CreateServiceRequest
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.model.UpdateServiceRequest

/**
 * Mapper for converting between Service DTOs and Domain models.
 */
object ServiceMapper {

    /**
     * Converts DTO to domain model.
     */
    fun toDomain(dto: ServiceDto): ProviderService = ProviderService(
        id = dto.id,
        name = dto.name,
        description = dto.description,
        basePrice = dto.basePrice,
        durationMinutes = dto.durationMinutes,
        categoryId = dto.categoryId,
        isActive = dto.isActive,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
    )

    /**
     * Converts list of DTOs to list of domain models.
     */
    fun toDomain(dtos: List<ServiceDto>): List<ProviderService> =
        dtos.map { toDomain(it) }

    /**
     * Converts domain CreateServiceRequest to DTO.
     */
    fun toDto(request: CreateServiceRequest): CreateServiceRequestDto =
        CreateServiceRequestDto(
            name = request.name,
            description = request.description,
            basePrice = request.basePrice,
            durationMinutes = request.durationMinutes,
            categoryId = request.categoryId,
        )

    /**
     * Converts domain UpdateServiceRequest to DTO.
     */
    fun toDto(request: UpdateServiceRequest): UpdateServiceRequestDto =
        UpdateServiceRequestDto(
            name = request.name,
            description = request.description,
            basePrice = request.basePrice,
            durationMinutes = request.durationMinutes,
            categoryId = request.categoryId,
            isActive = request.isActive,
        )
}
