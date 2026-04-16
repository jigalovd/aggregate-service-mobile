package com.aggregateservice.feature.booking.data.repository

import com.aggregateservice.feature.booking.data.api.BookingApiService
import com.aggregateservice.feature.booking.data.dto.CancelRequest
import com.aggregateservice.feature.booking.data.dto.CreateBookingRequest
import com.aggregateservice.feature.booking.data.dto.RescheduleRequest
import com.aggregateservice.feature.booking.data.mapper.BookingMapper
import com.aggregateservice.feature.booking.data.mapper.ServiceMapper
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Реализация BookingRepository.
 *
 * **Architecture:**
 * - Data layer реализует интерфейс Domain layer
 * - Использует BookingApiService для сетевых запросов
 * - Использует BookingMapper для преобразования DTO -> Domain
 *
 * @property apiService API сервис для бронирований
 */
class BookingRepositoryImpl(
    private val apiService: BookingApiService,
) : BookingRepository {
    override suspend fun createBooking(
        providerId: String,
        serviceIds: List<String>,
        startTime: Instant,
        notes: String?,
    ): Result<Booking> {
        val request =
            CreateBookingRequest(
                providerId = providerId,
                serviceIds = serviceIds,
                startTime = startTime,
                notes = notes,
            )

        return apiService.createBooking(request).fold(
            onSuccess = { dto -> Result.success(BookingMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun getBookingById(bookingId: String): Result<Booking> {
        return apiService.getBookingById(bookingId).fold(
            onSuccess = { dto -> Result.success(BookingMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun getClientBookings(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<Booking>> {
        return apiService.getClientBookings(status, page, pageSize).fold(
            onSuccess = { dtos -> Result.success(BookingMapper.toDomainList(dtos)) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun confirmBooking(bookingId: String): Result<Booking> {
        return apiService.confirmBooking(bookingId).fold(
            onSuccess = {
                getBookingById(bookingId).getOrNull()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Failed to re-fetch booking"))
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun cancelBooking(bookingId: String, reason: String?): Result<Booking> {
        val request = CancelRequest(cancellationReason = reason)

        return apiService.cancelBooking(bookingId, request).fold(
            onSuccess = {
                getBookingById(bookingId).getOrNull()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Failed to re-fetch booking"))
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun rescheduleBooking(
        bookingId: String,
        newStartTime: Instant,
    ): Result<Booking> {
        val request = RescheduleRequest(newStartTime = newStartTime)

        return apiService.rescheduleBooking(bookingId, request).fold(
            onSuccess = {
                getBookingById(bookingId).getOrNull()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Failed to re-fetch booking"))
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun getAvailableSlots(
        providerId: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        serviceIds: List<String>,
    ): Result<List<TimeSlot>> =
        withContext(Dispatchers.IO) {
            apiService.getAvailableSlots(providerId, fromDate, toDate, serviceIds).fold(
                onSuccess = { dtos -> Result.success(BookingMapper.toDomainSlots(dtos)) },
                onFailure = { error -> Result.failure(error) },
            )
        }

    override suspend fun getProviderServices(providerId: String): Result<List<BookingService>> {
        return apiService.getProviderServices(providerId).fold(
            onSuccess = { dtos -> Result.success(ServiceMapper.toDomain(dtos)) },
            onFailure = { error -> Result.failure(error) },
        )
    }
}
