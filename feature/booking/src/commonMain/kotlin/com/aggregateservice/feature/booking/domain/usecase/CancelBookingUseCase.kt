package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository

/**
 * UseCase для отмены бронирования.
 *
 * **Business Rules:**
 * - bookingId обязателен
 * - Клиент может отменить минимум за 2 часа до начала
 * - Мастер может отменить в любое время (проверка на backend)
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

        return repository.cancelBooking(bookingId, reason)
    }
}
