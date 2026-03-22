package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.repository.BookingRepository

/**
 * UseCase для получения бронирования по ID.
 *
 * **Usage:**
 * ```kotlin
 * val result = getBookingByIdUseCase("booking-123")
 * result.fold(
 *     onSuccess = { booking -> showBookingDetails(booking) },
 *     onFailure = { error -> showError(error) }
 * )
 * ```
 */
class GetBookingByIdUseCase(
    private val repository: BookingRepository,
) {
    suspend operator fun invoke(bookingId: String): Result<Booking> {
        // Validation: bookingId
        if (bookingId.isBlank()) {
            return Result.failure(
                AppError.ValidationError("bookingId", "Booking ID is required"),
            )
        }

        return repository.getBookingById(bookingId)
    }
}
