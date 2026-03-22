package com.aggregateservice.feature.booking.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.booking.data.dto.BookingDto
import com.aggregateservice.feature.booking.data.dto.CancelRequest
import com.aggregateservice.feature.booking.data.dto.CreateBookingRequest
import com.aggregateservice.feature.booking.data.dto.RescheduleRequest
import com.aggregateservice.feature.booking.data.dto.ServiceDto
import com.aggregateservice.feature.booking.data.dto.TimeSlotDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate

/**
 * API сервис для Booking Feature.
 *
 * **Responsibilities:**
 * - Выполнение HTTP запросов к Backend API
 * - Сериализация/десериализация DTO
 * - Возврат Result с DTO или ошибкой
 *
 * **Endpoints:**
 * - POST   /bookings                    - Создать бронирование
 * - GET    /bookings/{id}               - Получить бронирование
 * - GET    /bookings/client/{id}        - История клиента
 * - PATCH  /bookings/{id}/confirm       - Подтвердить
 * - PATCH  /bookings/{id}/cancel        - Отменить
 * - PATCH  /bookings/{id}/reschedule    - Перенести
 * - GET    /bookings/slots              - Доступные слоты
 *
 * @property client HTTP клиент (Ktor)
 */
class BookingApiService(
    private val client: HttpClient,
) {
    /**
     * Создаёт новое бронирование.
     *
     * **Endpoint:** POST /bookings
     */
    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.post("/bookings") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Получает бронирование по ID.
     *
     * **Endpoint:** GET /bookings/{id}
     */
    suspend fun getBookingById(bookingId: String): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.get("/bookings/$bookingId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получает историю бронирований клиента.
     *
     * **Endpoint:** GET /bookings/client/{clientId}
     */
    suspend fun getClientBookings(
        clientId: String,
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<BookingDto>> {
        return safeApiCall<List<BookingDto>> {
            client.get("/bookings/client/$clientId") {
                contentType(ContentType.Application.Json)
                status?.let { parameter("status", it) }
                parameter("page", page)
                parameter("pageSize", pageSize)
            }
        }
    }

    /**
     * Подтверждает бронирование (для мастера).
     *
     * **Endpoint:** PATCH /bookings/{id}/confirm
     */
    suspend fun confirmBooking(bookingId: String): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.patch("/bookings/$bookingId/confirm") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Отменяет бронирование.
     *
     * **Endpoint:** PATCH /bookings/{id}/cancel
     */
    suspend fun cancelBooking(bookingId: String, request: CancelRequest): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.patch("/bookings/$bookingId/cancel") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Переносит бронирование на другое время.
     *
     * **Endpoint:** PATCH /bookings/{id}/reschedule
     */
    suspend fun rescheduleBooking(bookingId: String, request: RescheduleRequest): Result<BookingDto> {
        return safeApiCall<BookingDto> {
            client.patch("/bookings/$bookingId/reschedule") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Получает доступные временные слоты.
     *
     * **Endpoint:** GET /bookings/slots
     */
    suspend fun getAvailableSlots(
        providerId: String,
        date: LocalDate,
        serviceIds: List<String>,
    ): Result<List<TimeSlotDto>> {
        return safeApiCall<List<TimeSlotDto>> {
            client.get("/bookings/slots") {
                contentType(ContentType.Application.Json)
                parameter("providerId", providerId)
                parameter("date", date.toString())
                serviceIds.forEach { parameter("serviceIds", it) }
            }
        }
    }

    /**
     * Получает список услуг мастера для бронирования.
     *
     * **Endpoint:** GET /providers/{providerId}/services
     *
     * **Feature Isolation:** Booking использует собственный метод
     * вместо зависимости от feature:catalog.
     */
    suspend fun getProviderServices(providerId: String): Result<List<ServiceDto>> {
        return safeApiCall<List<ServiceDto>> {
            client.get("/providers/$providerId/services") {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
