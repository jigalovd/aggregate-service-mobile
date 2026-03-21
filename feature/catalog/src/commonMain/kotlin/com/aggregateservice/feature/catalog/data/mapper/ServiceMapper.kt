package com.aggregateservice.feature.catalog.data.mapper
import com.aggregateservice.feature.catalog.data.dto.ServiceDto
import com.aggregateservice.feature.catalog.domain.model.Price
import com.aggregateservice.feature.catalog.domain.model.Service
import kotlinx.datetime.Instant

/**
 * Mapper для преобразования ServiceDto в Service.
 */
object ServiceMapper {
    /**
     * Преобразует ServiceDto в Service.
     *
     * @param dto DTO из API
     * @param currency Код валюты (по умолчанию "ILS")
     * @return Domain model
     */
    fun toDomain(dto: ServiceDto, currency: String = "ILS"): Service = Service(
        id = dto.id,
        providerId = dto.providerId,
        categoryId = dto.categoryId,
        name = dto.name,
        description = dto.description,
        price = Price(
            amount = dto.price,
            currency = currency,
        ),
        durationMinutes = dto.durationMinutes,
        isActive = dto.isActive,
        createdAt = dto.createdAt,
    )
}
