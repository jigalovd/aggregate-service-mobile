package com.aggregateservice.feature.catalog.data.mapper

import com.aggregateservice.core.api.models.PublicProviderServiceItemResponse
import com.aggregateservice.core.api.models.ServiceResponse
import com.aggregateservice.feature.catalog.domain.model.Price
import com.aggregateservice.feature.catalog.domain.model.Service

/**
 * Mapper for converting generated service DTOs to Service domain model.
 */
object ServiceMapper {
    /**
     * Converts PublicProviderServiceItemResponse (from provider detail) to Service.
     * This DTO includes categoryName and providerId.
     *
     * @param dto Generated DTO from provider services endpoint
     * @param currency Currency code (default "ILS")
     * @return Domain model
     */
    fun toDomain(dto: PublicProviderServiceItemResponse, currency: String = "ILS"): Service =
        Service(
            id = dto.id,
            providerId = dto.providerId ?: "",
            categoryId = dto.categoryId,
            categoryName = dto.categoryName.localized(),
            name = dto.title.localized(),
            description = dto.description?.localized(),
            price =
                Price(
                    amount = dto.basePrice / 100.0,
                    currency = currency,
                ),
            durationMinutes = dto.durationMinutes,
            isActive = dto.isActive,
            isCombinable = true,
            createdAt = dto.createdAt,
        )

    /**
     * Converts ServiceResponse (from standalone service endpoints) to Service.
     * This DTO does not include categoryName or providerId.
     *
     * @param dto Generated DTO from service endpoint
     * @param currency Currency code (default "ILS")
     * @return Domain model
     */
    fun toDomain(dto: ServiceResponse, currency: String = "ILS"): Service =
        Service(
            id = dto.id,
            providerId = "",
            categoryId = dto.categoryId,
            categoryName = "",
            name = dto.title.localized(),
            description = dto.description?.localized(),
            price =
                Price(
                    amount = (dto.basePrice ?: 0) / 100.0,
                    currency = dto.currency ?: currency,
                ),
            durationMinutes = dto.durationMinutes,
            isActive = dto.isActive ?: true,
            isCombinable = dto.isCombinable ?: true,
            createdAt = dto.createdAt,
        )
}
