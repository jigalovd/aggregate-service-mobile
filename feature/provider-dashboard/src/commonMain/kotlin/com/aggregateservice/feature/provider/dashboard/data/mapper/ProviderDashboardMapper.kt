package com.aggregateservice.feature.provider.dashboard.data.mapper

import com.aggregateservice.core.api.models.BookingItemResponse
import com.aggregateservice.core.api.models.BookingResponse
import com.aggregateservice.core.api.models.ProviderResponse
import com.aggregateservice.feature.provider.dashboard.domain.model.BookingStatus
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats

/**
 * Mapper для преобразования API DTO в Domain модели Provider Dashboard.
 *
 * Источник истины: OpenAPI spec (core:api-models).
 * Booking endpoints используются как proxy для provider dashboard данных.
 *
 * **Mapping Rules:**
 * - BookingResponse.status → BookingStatus (case-insensitive parsing)
 * - BookingResponse.items.firstOrNull()?.serviceName → DashboardBooking.serviceName
 * - ProviderResponse → ProviderStats (pendingRequests derived from status counts)
 */
object ProviderDashboardMapper {
    /**
     * Преобразует BookingResponse в DashboardBooking.
     *
     * Использует первый item из списка для получения serviceName.
     * Если items пустой, serviceName устанавливается в "Unknown Service".
     *
     * @param dto API DTO бронирования
     * @return Domain модель для отображения в dashboard
     */
    fun toDashboardBooking(dto: BookingResponse): DashboardBooking {
        val firstItem = dto.items?.firstOrNull()
        val clientName = extractClientName(dto)

        return DashboardBooking(
            id = dto.id,
            clientName = clientName,
            startTime = dto.startTime,
            endTime = dto.endTime,
            serviceName = firstItem?.serviceName ?: "Unknown Service",
            status = parseStatus(dto.status),
            totalPrice = dto.totalPrice ?: 0.0,
        )
    }

    /**
     * Преобразует список BookingResponse в список DashboardBooking.
     *
     * @param dtos Список API DTO
     * @return Список доменных моделей
     */
    fun toDashboardBookingList(dtos: List<BookingResponse>): List<DashboardBooking> {
        return dtos.map { toDashboardBooking(it) }
    }

    /**
     * Преобразует ProviderResponse в ProviderStats.
     *
     * Статистика pending requests вычисляется на основе данных профиля.
     * В будущем, когда появится dedicated endpoint с count pending requests,
     * эта логика будет обновлена.
     *
     * @param dto API DTO профиля провайдера
     * @return Domain модель статистики
     */
    fun toProviderStats(dto: ProviderResponse): ProviderStats {
        // TODO: When backend provides /api/v1/providers/me/stats endpoint,
        // replace this with real data from the dedicated endpoint.
        // Current implementation returns empty stats - pendingRequests,
        // activeBookings, completedToday will be populated from bookings data.
        return ProviderStats(
            pendingRequests = 0, // Will be computed from bookings
            activeBookings = 0,   // Will be computed from bookings
            completedToday = 0,   // Will be computed from bookings
        )
    }

    /**
     * Вычисляет ProviderStats из списка бронирований.
     *
     * Анализирует список today's bookings для подсчёта:
     * - pendingRequests: бронирования со статусом PENDING
     * - activeBookings: бронирования со статусом CONFIRMED или IN_PROGRESS
     * - completedToday: бронирования со статусом COMPLETED
     *
     * @param bookings Список бронирований провайдера
     * @return Вычисленная статистика
     */
    fun computeStatsFromBookings(bookings: List<DashboardBooking>): ProviderStats {
        return ProviderStats(
            pendingRequests = bookings.count { it.status == BookingStatus.PENDING },
            activeBookings = bookings.count {
                it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.IN_PROGRESS
            },
            completedToday = bookings.count { it.status == BookingStatus.COMPLETED },
        )
    }

    /**
     * Извлекает имя клиента из BookingResponse.
     *
     * В текущей версии API clientId не содержит displayName.
     * Используется "Client" как fallback. В будущем, когда появится
     * endpoint для получения client details, будет обновлено.
     *
     * @param dto BookingResponse с данными бронирования
     * @return Имя клиента для отображения
     */
    private fun extractClientName(dto: BookingResponse): String {
        // TODO: When backend provides /api/v1/users/{id} or similar endpoint,
        // fetch client name from user profile.
        // Currently API doesn't expose client display name in booking response.
        return "Client" // Placeholder until API provides client name
    }

    /**
     * Парсит статус из строки.
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