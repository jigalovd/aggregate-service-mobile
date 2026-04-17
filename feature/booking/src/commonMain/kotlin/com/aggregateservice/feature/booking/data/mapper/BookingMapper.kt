package com.aggregateservice.feature.booking.data.mapper

import com.aggregateservice.core.api.models.BookingAvailableSlotResponse
import com.aggregateservice.core.api.models.BookingItemResponse
import com.aggregateservice.core.api.models.BookingResponse
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingItem
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.domain.model.TimeSlot

/**
 * Mapper для преобразования сгенерированных API DTO в Domain модели.
 *
 * Источник истины: OpenAPI spec (core:api-models).
 */
object BookingMapper {
    fun toDomain(dto: BookingResponse): Booking =
        Booking(
            id = dto.id,
            providerId = dto.providerId,
            providerName = dto.providerName ?: "",
            clientId = dto.clientId,
            startTime = dto.startTime,
            endTime = dto.endTime,
            status = parseStatus(dto.status),
            items = (dto.items ?: emptyList()).map { toDomain(it) },
            totalPrice = dto.totalPrice ?: 0.0,
            totalDurationMinutes = dto.totalDurationMinutes,
            currency = dto.currency ?: "ILS",
            notes = dto.notes,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )

    private fun toDomain(dto: BookingItemResponse): BookingItem =
        BookingItem(
            id = dto.id,
            serviceId = dto.serviceId,
            serviceName = dto.serviceName,
            price = dto.price,
            currency = dto.currency ?: "ILS",
            durationMinutes = dto.durationMinutes,
        )

    fun toDomain(dto: BookingAvailableSlotResponse): TimeSlot =
        TimeSlot(
            startTime = dto.startTime,
            endTime = dto.endTime,
            isAvailable = dto.isAvailable ?: true,
            providerId = dto.providerId,
        )

    fun toDomainList(dtos: List<BookingResponse>): List<Booking> = dtos.map { toDomain(it) }

    fun toDomainSlots(dtos: List<BookingAvailableSlotResponse>): List<TimeSlot> = dtos.map { toDomain(it) }

    private fun parseStatus(status: String): BookingStatus {
        return try {
            BookingStatus.valueOf(status.uppercase())
        } catch (_: IllegalArgumentException) {
            BookingStatus.PENDING
        }
    }
}
