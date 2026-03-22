package com.aggregateservice.feature.booking.data.mapper

import com.aggregateservice.feature.booking.data.dto.ServiceDto
import com.aggregateservice.feature.booking.domain.model.BookingService

/**
 * Маппер для преобразования ServiceDto → BookingService.
 *
 * **Feature Isolation:** Преобразует DTO в собственную модель booking.
 */
object ServiceMapper {

    /**
     * Преобразует DTO в доменную модель.
     */
    fun toDomain(dto: ServiceDto): BookingService = BookingService(
        id = dto.id,
        name = dto.name,
        description = dto.description,
        price = dto.price,
        currency = dto.currency,
        durationMinutes = dto.durationMinutes,
    )

    /**
     * Преобразует список DTO в список доменных моделей.
     */
    fun toDomain(dtos: List<ServiceDto>): List<BookingService> =
        dtos.map { toDomain(it) }
}
