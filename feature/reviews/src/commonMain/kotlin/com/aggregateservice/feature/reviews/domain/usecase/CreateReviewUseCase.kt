package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
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
                AppError.FormValidation(
                    field = "bookingId",
                    rule = ValidationRule.Required,
                ),
            )
        }
        if (rating !in MIN_RATING..MAX_RATING) {
            return Result.failure(
                AppError.FormValidation(
                    field = "rating",
                    rule = ValidationRule.TooLow,
                    parameters = mapOf("min" to MIN_RATING, "max" to MAX_RATING),
                )
            )
        }

        // Trim comment if provided
        val trimmedComment = comment?.trim()?.takeIf { it.isNotBlank() }

        // ADD: Validate booking status is COMPLETED (defense-in-depth)
        val canReview =
            repository.canReviewBooking(bookingId).getOrElse {
                return Result.failure(it)
            }
        if (!canReview) {
            return Result.failure(
                AppError.FormValidation(
                    field = "bookingId",
                    rule = ValidationRule.InvalidValue,
                ),
            )
        }

        return repository.createReview(bookingId, rating, trimmedComment)
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
