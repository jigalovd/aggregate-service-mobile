package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.datetime.Instant

/**
 * UseCase для переноса бронирования на другое время.
 *
 * **Business Rules:**
 * - bookingId обязателен
 * - Новое время должно быть в будущем
 * - Клиент может перенести минимум за 2 часа до начала
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
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        if (newStartTime <= now) {
            return Result.failure(
                AppError.ValidationError("newStartTime", "New start time must be in the future"),
            )
        }

        return repository.rescheduleBooking(bookingId, newStartTime)
    }
}
