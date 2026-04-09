@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

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
                AppError.FormValidation("bookingId", ValidationRule.Required),
            )
        }

        // Get booking to check time window
        val booking =
            repository.getBookingById(bookingId).getOrElse { error ->
                return Result.failure(error)
            }

        // Validation: booking must be cancellable status
        if (!booking.status.isCancellable) {
            return Result.failure(
                AppError.FormValidation(
                    "status",
                    ValidationRule.InvalidValue,
                    mapOf("status" to booking.status.name),
                ),
            )
        }

        // Validation: 2-hour window before start time (US-3.5)
        val now = Clock.System.now()
        val minCancelTime = booking.startTime.minus(CANCEL_WINDOW_HOURS.toInt().hours)

        if (now > minCancelTime) {
            return Result.failure(
                AppError.FormValidation(
                    "startTime",
                    ValidationRule.TooLow,
                    mapOf("min" to CANCEL_WINDOW_HOURS),
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
