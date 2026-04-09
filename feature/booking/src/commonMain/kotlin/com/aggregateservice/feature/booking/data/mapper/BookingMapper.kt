package com.aggregateservice.feature.booking.data.mapper

import com.aggregateservice.feature.booking.data.dto.BookingDto
import com.aggregateservice.feature.booking.data.dto.BookingItemDto
import com.aggregateservice.feature.booking.data.dto.TimeSlotDto
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingItem
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.domain.model.TimeSlot

/**
 * Mapper для преобразования DTO в Domain модели.
 *
 * **Architecture:**
 * - Data layer использует этот mapper для конвертации
 * - Domain layer не зависит от DTO
 * - Однонаправленный поток: DTO -> Domain
 */
object BookingMapper {
    /**
     * Преобразует BookingDto в Booking domain model.
     */
    fun toDomain(dto: BookingDto): Booking =
        Booking(
            id = dto.id,
            providerId = dto.providerId,
            providerName = dto.providerName,
            clientId = dto.clientId,
            startTime = dto.startTime,
            endTime = dto.endTime,
            status = parseStatus(dto.status),
            items = dto.items.map { toDomain(it) },
            totalPrice = dto.totalPrice,
            totalDurationMinutes = dto.totalDurationMinutes,
            currency = dto.currency,
            notes = dto.notes,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )

    /**
     * Преобразует BookingItemDto в BookingItem domain model.
     */
    private fun toDomain(dto: BookingItemDto): BookingItem =
        BookingItem(
            id = dto.id,
            serviceId = dto.serviceId,
            serviceName = dto.serviceName,
            price = dto.price,
            currency = dto.currency,
            durationMinutes = dto.durationMinutes,
        )

    /**
     * Преобразует TimeSlotDto в TimeSlot domain model.
     */
    fun toDomain(dto: TimeSlotDto): TimeSlot =
        TimeSlot(
            startTime = dto.startTime,
            endTime = dto.endTime,
            isAvailable = dto.isAvailable,
            providerId = dto.providerId,
        )

    /**
     * Преобразует список BookingDto в список Booking.
     */
    fun toDomainList(dtos: List<BookingDto>): List<Booking> = dtos.map { toDomain(it) }

    /**
     * Преобразует список TimeSlotDto в список TimeSlot.
     */
    fun toDomainSlots(dtos: List<TimeSlotDto>): List<TimeSlot> = dtos.map { toDomain(it) }

    /**
     * Парсит строковый статус в enum BookingStatus.
     */
    private fun parseStatus(status: String): BookingStatus {
        return try {
            BookingStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            BookingStatus.PENDING // Default fallback
        }
    }
}
