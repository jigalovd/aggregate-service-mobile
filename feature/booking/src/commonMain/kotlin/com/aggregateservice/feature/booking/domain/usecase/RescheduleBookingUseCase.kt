package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * UseCase для переноса бронирования на другое время.
 *
 * **Business Rules:**
 * - bookingId обязателен
 * - Новое время должно быть в будущем
 * - Клиент может перенести минимум за 2 часа до начала (US-3.11)
 * - Максимум 3 переноса на одно бронирование (проверка на backend)
 *
 * **Usage:**
 * ```kotlin
 * val result = rescheduleBookingUseCase(
 *     bookingId = "booking-123",
 *     newStartTime = Instant.parse("2026-03-26T15:00:00Z")
 * )
 * ```
 */
class RescheduleBookingUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(
        bookingId: String,
        newStartTime: Instant,
    ): Result<Booking> {
        // Validation: bookingId
        if (bookingId.isBlank()) {
            return Result.failure(
                AppError.ValidationError("bookingId", "Booking ID is required"),
            )
        }

        // Validation: newStartTime must be in the future
        val now = Clock.System.now()
        if (newStartTime <= now) {
            return Result.failure(
                AppError.ValidationError("newStartTime", "New start time must be in the future"),
            )
        }

        // Получаем бронирование для проверки времени (US-3.11)
        val booking = repository.getBookingById(bookingId).getOrElse { error ->
            return Result.failure(error)
        }

        // Validation: 2-hour window before start time (US-3.11)
        // Клиент может перенести минимум за 2 часа до начала
        val minRescheduleTime = Instant.fromEpochMilliseconds(
            booking.startTime.toEpochMilliseconds() - RESCHEDULE_WINDOW_HOURS * 60 * 60 * 1000,
        )

        if (now > minRescheduleTime) {
            return Result.failure(
                AppError.ValidationError(
                    "startTime",
                    "Cannot reschedule booking less than $RESCHEDULE_WINDOW_HOURS hours before start time",
                ),
            )
        }

        return repository.rescheduleBooking(bookingId, newStartTime)
    }

    companion object {
        /**
         * Минимальное время до начала бронирования для переноса (в часах).
         * US-3.11: Клиент может перенести минимум за 2 часа до начала.
         */
        const val RESCHEDULE_WINDOW_HOURS = 2L
    }
}
