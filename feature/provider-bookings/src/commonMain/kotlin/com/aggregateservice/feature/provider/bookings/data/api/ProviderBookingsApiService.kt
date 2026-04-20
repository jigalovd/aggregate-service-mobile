package com.aggregateservice.feature.provider.bookings.data.api

import com.aggregateservice.core.api.models.BookingCancel
import com.aggregateservice.core.api.models.BookingResponse
import com.aggregateservice.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API Service для управления бронированиями провайдера.
 *
 * Использует HttpClient для взаимодействия с backend API.
 *
 * **Endpoints:**
 * - GET /api/v1/bookings/provider/me — список бронирований провайдера
 * - PATCH /api/v1/bookings/{id}/confirm — подтвердить бронирование
 * - PATCH /api/v1/bookings/{id}/cancel — отменить бронирование
 * - PATCH /api/v1/bookings/{id}/reject — отклонить бронирование
 */
class ProviderBookingsApiService(
    private val client: HttpClient,
) {
    /**
     * Получает список бронирований текущего провайдера.
     *
     * @param status Фильтр по статусу (null = все статусы)
     * @param page Номер страницы (начиная с 1)
     * @param pageSize Размер страницы
     * @return Result со списком бронирований или ошибкой
     */
    suspend fun getProviderBookings(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<BookingResponse>> {
        return safeApiCall<List<BookingResponse>> {
            client.get("/api/v1/bookings/provider/me") {
                contentType(ContentType.Application.Json)
                url {
                    status?.let { parameters.append("status", it) }
                    parameters.append("page", page.toString())
                    parameters.append("pageSize", pageSize.toString())
                }
            }
        }
    }

    /**
     * Подтверждает бронирование (изменяет статус на CONFIRMED).
     *
     * @param bookingId ID бронирования
     * @return Result с подтверждением или ошибкой
     */
    suspend fun confirmBooking(bookingId: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/confirm") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Отклоняет бронирование.
     *
     * @param bookingId ID бронирования
     * @param reason Причина отклонения
     * @return Result с подтверждением или ошибкой
     */
    suspend fun rejectBooking(
        bookingId: String,
        reason: String,
    ): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/reject") {
                contentType(ContentType.Application.Json)
                setBody(BookingCancel(cancellationReason = reason))
            }
        }
    }

    /**
     * Отменяет бронирование.
     *
     * @param bookingId ID бронирования
     * @param request Данные об отмене (причина опционально)
     * @return Result с подтверждением или ошибкой
     */
    suspend fun cancelBooking(
        bookingId: String,
        request: BookingCancel,
    ): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/cancel") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}
