package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * UseCase для отмены бронирования.
 *
 * **Business Rules (US-3.5):**
 * - bookingId обязателен
 * - Клиент может отменить минимум за 2 часа до начала (CANCEL_WINDOW_HOURS)
 * - Мастер может отменить в любое время (проверка на backend)
 * - Отмена невозможна после начала бронирования
 *
 * **Usage:**
 * ```kotlin
 * val result = cancelBookingUseCase(
 *     bookingId = "booking-123",
 *     reason = "Не смог прийти"
 * )
 * ```
 */
class CancelBookingUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        bookingId: String,
        reason: String? = null,
    ): Result<Booking> {
        // Validation: bookingId
        if (bookingId.isBlank()) {
            return Result.failure(
                AppError.ValidationError("bookingId", "Booking ID is required"),
            )
        }

        // Get booking to check time window
        val booking = repository.getBookingById(bookingId).getOrElse { error ->
            return Result.failure(error)
        }

        // Validation: booking must be cancellable status
        if (!booking.status.isCancellable) {
            return Result.failure(
                AppError.ValidationError(
                    "status",
                    "Booking in status '${booking.status}' cannot be cancelled",
                ),
            )
        }

        // Validation: 2-hour window before start time (US-3.5)
        val now = Clock.System.now()
        val minCancelTime = Instant.fromEpochMilliseconds(
            booking.startTime.toEpochMilliseconds() - CANCEL_WINDOW_HOURS * 60 * 60 * 1000,
        )

        if (now > minCancelTime) {
            return Result.failure(
                AppError.ValidationError(
                    "startTime",
                    "Cannot cancel booking less than $CANCEL_WINDOW_HOURS hours before start time",
                ),
            )
        }

        return repository.cancelBooking(bookingId, reason)
    }

    companion object {
        /**
         * Minimum hours before start time for cancellation.
         * Business Rule: US-3.5
         */
        const val CANCEL_WINDOW_HOURS = 2L
    }
}
