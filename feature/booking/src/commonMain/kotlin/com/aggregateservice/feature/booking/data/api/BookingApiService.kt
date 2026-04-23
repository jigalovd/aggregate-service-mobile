package com.aggregateservice.feature.booking.data.api

import com.aggregateservice.core.api.models.BookingAvailableSlotResponse
import com.aggregateservice.core.api.models.BookingCancel
import com.aggregateservice.core.api.models.BookingCreate
import com.aggregateservice.core.api.models.BookingReschedule
import com.aggregateservice.core.api.models.BookingResponse
import com.aggregateservice.core.api.models.PublicProviderServiceItemResponse
import com.aggregateservice.core.api.models.PublicProviderServicesResponse
import com.aggregateservice.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate

class BookingApiService(
    private val client: HttpClient,
) {
    suspend fun createBooking(request: BookingCreate): Result<BookingResponse> {
        return safeApiCall<BookingResponse> {
            client.post("/api/v1/bookings") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun getBookingById(bookingId: String): Result<BookingResponse> {
        return safeApiCall<BookingResponse> {
            client.get("/api/v1/bookings/$bookingId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    suspend fun getClientBookings(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<BookingResponse>> {
        return safeApiCall<List<BookingResponse>> {
            client.get("/api/v1/bookings/client/me") {
                contentType(ContentType.Application.Json)
                url {
                    status?.let { parameters.append("status", it) }
                    // Backend API uses limit/offset (not page/pageSize)
                    parameters.append("limit", pageSize.toString())
                    parameters.append("offset", ((page - 1) * pageSize).toString())
                }
            }
        }
    }

    suspend fun confirmBooking(bookingId: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/confirm") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    suspend fun cancelBooking(bookingId: String, request: BookingCancel): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/cancel") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun rescheduleBooking(bookingId: String, request: BookingReschedule): Result<Unit> {
        return safeApiCall<Unit> {
            client.patch("/api/v1/bookings/$bookingId/reschedule") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun getAvailableSlots(
        providerId: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        serviceIds: List<String>,
    ): Result<List<BookingAvailableSlotResponse>> {
        return safeApiCall<List<BookingAvailableSlotResponse>> {
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

    suspend fun getProviderServices(providerId: String): Result<List<PublicProviderServiceItemResponse>> {
        return safeApiCall<PublicProviderServicesResponse> {
            client.get("/api/v1/catalog/providers/$providerId/services") {
                contentType(ContentType.Application.Json)
            }
        }.map { it.services }
    }
}
