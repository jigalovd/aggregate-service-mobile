package com.aggregateservice.feature.booking.data.mapper

import com.aggregateservice.core.api.models.I18nStringSchema
import com.aggregateservice.core.api.models.PublicProviderServiceItemResponse
import com.aggregateservice.feature.booking.domain.model.BookingService

/**
 * Маппер для преобразования PublicProviderServiceItemResponse → BookingService.
 *
 * Извлекает плоские строки из I18nStringSchema (priority: ru → he → en).
 */
object ServiceMapper {
    fun toDomain(dto: PublicProviderServiceItemResponse): BookingService =
        BookingService(
            id = dto.id,
            name = dto.title.localized(),
            description = dto.description?.localized(),
            price = dto.basePrice / 100.0,
            currency = "ILS",
            durationMinutes = dto.durationMinutes,
            isCombinable = true,
        )

    fun toDomain(dtos: List<PublicProviderServiceItemResponse>): List<BookingService> =
        dtos.map { toDomain(it) }

    private fun I18nStringSchema.localized(): String =
        ru.ifBlank { he.ifBlank { en } }
}
