package com.aggregateservice.feature.provider.bookings.data.mapper

import com.aggregateservice.core.api.models.BookingResponse
import com.aggregateservice.feature.provider.bookings.domain.model.BookingStatus
import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking

/**
 * Mapper для преобразования BookingResponse DTO в ProviderBooking domain model.
 *
 * Источник истины: OpenAPI spec (core:api-models).
 *
 * **Mapping Rules:**
 * - BookingResponse.id → ProviderBooking.id
 * - BookingResponse.providerName → ProviderBooking.clientName (snapshot из booking)
 * - BookingResponse.startTime → ProviderBooking.startTime
 * - BookingResponse.endTime → ProviderBooking.endTime
 * - BookingResponse.items.firstOrNull()?.serviceName → ProviderBooking.serviceName
 * - BookingResponse.totalPrice → ProviderBooking.totalPrice
 * - BookingResponse.status → BookingStatus (case-insensitive)
 * - BookingResponse.notes → ProviderBooking.notes
 */
object ProviderBookingMapper {

    /**
     * Преобразует BookingResponse в ProviderBooking.
     *
     * @param dto API DTO бронирования
     * @return Domain модель для UI списка бронирований провайдера
     */
    fun toProviderBooking(dto: BookingResponse): ProviderBooking {
        val firstItem = dto.items?.firstOrNull()
        val serviceName = firstItem?.serviceName ?: "Unknown Service"

        return ProviderBooking(
            id = dto.id,
            clientName = dto.providerName ?: "Client",
            startTime = dto.startTime,
            endTime = dto.endTime,
            serviceName = serviceName,
            status = parseStatus(dto.status),
            totalPrice = dto.totalPrice ?: 0.0,
            clientPhone = null, // API doesn't expose client phone in booking response
            notes = dto.notes,
        )
    }

    /**
     * Преобразует список BookingResponse в список ProviderBooking.
     *
     * @param dtos Список API DTO
     * @return Список domain моделей
     */
    fun toProviderBookingList(dtos: List<BookingResponse>): List<ProviderBooking> {
        return dtos.map { toProviderBooking(it) }
    }

    /**
     * Парсит статус из строки (case-insensitive).
     *
     * @param status Строка статуса из API
     * @return Доменный BookingStatus
     */
    private fun parseStatus(status: String): BookingStatus {
        return try {
            BookingStatus.valueOf(status.uppercase())
        } catch (_: IllegalArgumentException) {
            // Map unknown statuses to most common default
            BookingStatus.PENDING
        }
    }
}
