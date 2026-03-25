package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository

/**
 * Use case for creating a new review.
 *
 * Validates:
 * - Rating must be between 1 and 5
 * - Booking ID must not be empty
 *
 * @property repository Reviews repository
 */
class CreateReviewUseCase(
    private val repository: ReviewsRepository,
) {
    suspend operator fun invoke(
        bookingId: String,
        rating: Int,
        comment: String? = null,
    ): Result<Review> {
        // Validation
        if (bookingId.isBlank()) {
            return Result.failure(
                AppError.ValidationError(
                    field = "bookingId",
                    message = "Booking ID cannot be empty",
                ),
            )
        }
        if (rating !in MIN_RATING..MAX_RATING) {
            return Result.failure(
                AppError.ValidationError(
                    field = "rating",
                    message = "Rating must be between $MIN_RATING and $MAX_RATING",
                ),
            )
        }

        // Trim comment if provided
        val trimmedComment = comment?.trim()?.takeIf { it.isNotBlank() }

        return repository.createReview(bookingId, rating, trimmedComment)
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
