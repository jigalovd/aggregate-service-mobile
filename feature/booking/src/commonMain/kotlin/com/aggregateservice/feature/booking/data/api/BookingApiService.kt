package com.aggregateservice.feature.booking.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.booking.data.dto.BookingDto
import com.aggregateservice.feature.booking.data.dto.CancelRequest
import com.aggregateservice.feature.booking.data.dto.CreateBookingRequest
import com.aggregateservice.feature.booking.data.dto.RescheduleRequest
import com.aggregateservice.feature.booking.data.dto.ServiceDto
import com.aggregateservice.feature.booking.data.dto.ServiceListResponseDto
import com.aggregateservice.feature.booking.data.dto.TimeSlotDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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
 * - POST   /bookings                    - Создать бронирование (auth)
 * - GET    /bookings/{id}               - Получить бронирование (auth)
 * - GET    /bookings/client/{id}        - История клиента (auth)
 * - PATCH  /bookings/{id}/confirm       - Подтвердить (auth)
 * - PATCH  /bookings/{id}/cancel        - Отменить (auth)
 * - PATCH  /bookings/{id}/reschedule    - Перенести (auth)
 * - GET    /bookings/slots              - Доступные слоты (auth)
 *
 * **Auth:** Ktor Auth Plugin handles Authorization header automatically
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
            client.post("/api/v1/bookings") {
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
            client.get("/api/v1/bookings/$bookingId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получает историю бронирований клиента.
     *
     * **Endpoint:** GET /bookings/client/me
     */
    suspend fun getClientBookings(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<BookingDto>> {
        return safeApiCall<List<BookingDto>> {
            client.get("/api/v1/bookings/client/me") {
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
     * Подтверждает бронирование (для мастера).
     *
     * **Endpoint:** PATCH /bookings/{id}/confirm
     */
    suspend fun confirmBooking(bookingId: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/confirm") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Отменяет бронирование.
     *
     * **Endpoint:** PATCH /bookings/{id}/cancel
     */
    suspend fun cancelBooking(bookingId: String, request: CancelRequest): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/cancel") {
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
    suspend fun rescheduleBooking(bookingId: String, request: RescheduleRequest): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/reschedule") {
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
        fromDate: LocalDate,
        toDate: LocalDate,
        serviceIds: List<String>,
    ): Result<List<TimeSlotDto>> {
        return safeApiCall<List<TimeSlotDto>> {
            client.get("/api/v1/bookings/slots") {
                contentType(ContentType.Application.Json)
                url {
                    parameters.append("providerId", providerId)
                    parameters.append("fromDate", fromDate.toString())
                    parameters.append("toDate", toDate.toString())
                    if (serviceIds.isNotEmpty()) {
                        parameters.append("serviceIds", serviceIds.joinToString(","))
                    }
                }
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
        return safeApiCall<ServiceListResponseDto> {
            client.get("/api/v1/catalog/providers/$providerId/services") {
                contentType(ContentType.Application.Json)
            }
        }.map { it.services }
    }
}
