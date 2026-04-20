package com.aggregateservice.feature.provider.bookings.data.repository

import co.touchlab.kermit.Logger
import com.aggregateservice.core.api.models.BookingCancel
import com.aggregateservice.feature.provider.bookings.data.api.ProviderBookingsApiService
import com.aggregateservice.feature.provider.bookings.data.mapper.ProviderBookingMapper
import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking
import com.aggregateservice.feature.provider.bookings.domain.repository.ProviderBookingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Реализация ProviderBookingRepository.
 *
 * Использует ProviderBookingsApiService для сетевых запросов
 * и ProviderBookingMapper для преобразования DTO → Domain.
 *
 * **Architecture:**
 * - Data layer реализует интерфейс Domain layer
 * - Все сетевые операции выполняются в Dispatchers.IO
 * - Logger tag "ProviderBookings" для observability
 * - Result.fold() для обработки ошибок с логированием
 *
 * @property apiService API сервис для бронирований
 * @property logger Логгер для observability (tag: "ProviderBookings")
 */
class ProviderBookingRepositoryImpl(
    private val apiService: ProviderBookingsApiService,
    private val logger: Logger,
) : ProviderBookingRepository {

    override suspend fun getProviderBookings(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<ProviderBooking>> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderBookings") {
                "getProviderBookings(status=$status, page=$page, pageSize=$pageSize)"
            }

            apiService.getProviderBookings(status, page, pageSize).fold(
                onSuccess = { dtos ->
                    val bookings = ProviderBookingMapper.toProviderBookingList(dtos)
                    logger.d("ProviderBookings") {
                        "getProviderBookings: fetched ${bookings.size} bookings"
                    }
                    Result.success(bookings)
                },
                onFailure = { error ->
                    logger.e("ProviderBookings") {
                        "getProviderBookings: failed - ${error.message}"
                    }
                    Result.failure(error)
                },
            )
        }
    }

    override suspend fun acceptBooking(bookingId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderBookings") {
                "acceptBooking(id=$bookingId)"
            }

            apiService.confirmBooking(bookingId).fold(
                onSuccess = {
                    logger.d("ProviderBookings") {
                        "acceptBooking: success for booking $bookingId"
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    logger.e("ProviderBookings") {
                        "acceptBooking: failed for booking $bookingId - ${error.message}"
                    }
                    Result.failure(error)
                },
            )
        }
    }

    override suspend fun rejectBooking(bookingId: String, reason: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderBookings") {
                "rejectBooking(id=$bookingId, reason=$reason)"
            }

            apiService.rejectBooking(bookingId, reason).fold(
                onSuccess = {
                    logger.d("ProviderBookings") {
                        "rejectBooking: success for booking $bookingId"
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    logger.e("ProviderBookings") {
                        "rejectBooking: failed for booking $bookingId - ${error.message}"
                    }
                    Result.failure(error)
                },
            )
        }
    }

    override suspend fun cancelBooking(bookingId: String, reason: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderBookings") {
                "cancelBooking(id=$bookingId, reason=$reason)"
            }

            apiService.cancelBooking(bookingId, BookingCancel(cancellationReason = reason)).fold(
                onSuccess = {
                    logger.d("ProviderBookings") {
                        "cancelBooking: success for booking $bookingId"
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    logger.e("ProviderBookings") {
                        "cancelBooking: failed for booking $bookingId - ${error.message}"
                    }
                    Result.failure(error)
                },
            )
        }
    }
}
