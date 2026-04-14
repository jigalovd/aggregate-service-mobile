package com.aggregateservice.feature.booking.data.mapper

import com.aggregateservice.feature.booking.data.dto.ServiceDto
import com.aggregateservice.feature.booking.domain.model.BookingService

/**
 * Маппер для преобразования ServiceDto → BookingService.
 *
 * Извлекает плоские строки из i18n-полей DTO (priority: ru → he → en).
 */
object ServiceMapper {
    /**
     * Преобразует DTO в доменную модель.
     */
    fun toDomain(dto: ServiceDto): BookingService =
        BookingService(
            id = dto.id,
            name = dto.titleMap["ru"] ?: dto.titleMap["he"] ?: dto.titleMap["en"] ?: "",
            description = dto.descriptionMap?.let {
                it["ru"] ?: it["he"] ?: it["en"]
            },
            price = dto.priceInCents.toDouble(),
            currency = "ILS",
            durationMinutes = dto.durationMinutes,
            isCombinable = dto.isCombinable,
        )

    /**
     * Преобразует список DTO в список доменных моделей.
     */
    fun toDomain(dtos: List<ServiceDto>): List<BookingService> =
        dtos.map { toDomain(it) }
}
