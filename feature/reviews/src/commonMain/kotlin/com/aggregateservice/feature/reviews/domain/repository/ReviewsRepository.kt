package com.aggregateservice.feature.reviews.domain.repository

import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats

/**
 * Repository interface for reviews.
 *
 * Follows Clean Architecture - this interface belongs to the domain layer.
 * Implementation is in the data layer.
 */
interface ReviewsRepository {
    /**
     * Get paginated reviews for a provider.
     *
     * @param providerId The provider's ID
     * @param page Page number (1-based)
     * @param pageSize Number of items per page
     * @return Result with list of reviews or error
     */
    suspend fun getProviderReviews(
        providerId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<Review>>

    /**
     * Get review statistics for a provider.
     *
     * @param providerId The provider's ID
     * @return Result with review stats or error
     */
    suspend fun getReviewStats(providerId: String): Result<ReviewStats>

    /**
     * Check if a booking can be reviewed.
     * Used to prevent fake reviews (one review per booking).
     *
     * @param bookingId The booking's ID
     * @return Result with true if review is allowed, false otherwise
     */
    suspend fun canReviewBooking(bookingId: String): Result<Boolean>

    /**
     * Create a new review for a booking.
     *
     * @param bookingId The booking's ID
     * @param rating Rating from 1 to 5
     * @param comment Optional comment text
     * @return Result with created review or error
     */
    suspend fun createReview(
        bookingId: String,
        rating: Int,
        comment: String?,
    ): Result<Review>
}
