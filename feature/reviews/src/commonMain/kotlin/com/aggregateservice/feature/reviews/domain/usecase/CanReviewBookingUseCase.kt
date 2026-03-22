package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository

/**
 * Use case for checking if a booking can be reviewed.
 *
 * This is part of the anti-fake-review mechanism:
 * - A review can only be created for a completed booking
 * - Only one review per booking is allowed
 */
class CanReviewBookingUseCase(
    private val repository: ReviewsRepository,
) {
    suspend operator fun invoke(bookingId: String): Result<Boolean> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("Booking ID cannot be empty"))
        }

        return repository.canReviewBooking(bookingId)
    }
}
