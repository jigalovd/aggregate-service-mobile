package com.aggregateservice.feature.reviews.data.mapper

import com.aggregateservice.core.api.models.CanReviewResponse
import com.aggregateservice.core.api.models.ReviewResponse
import com.aggregateservice.core.api.models.ReviewStatsResponse
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats

/**
 * Mapper for converting generated API DTOs to Domain models.
 *
 * Source of truth: OpenAPI spec (core:api-models).
 */
object ReviewMapper {
    /**
     * Maps a generated [ReviewResponse] to a domain [Review].
     *
     * Note: The generated [ReviewResponse] does not include providerName, clientName,
     * or providerReply fields from the API. These are defaulted to empty/null until
     * the OpenAPI spec is updated to include them.
     */
    fun toDomain(dto: ReviewResponse): Review =
        Review(
            id = dto.id,
            bookingId = dto.bookingId,
            providerId = dto.providerId,
            providerName = "",
            clientId = dto.clientId,
            clientName = "",
            rating = dto.rating,
            comment = dto.comment,
            providerReply = null,
            createdAt = dto.createdAt,
        )

    /**
     * Maps a generated [ReviewStatsResponse] to a domain [ReviewStats].
     *
     * Note: The generated [ReviewStatsResponse] does not include providerId or
     * ratingDistribution. providerId must be provided by the caller and
     * ratingDistribution is defaulted to an empty map until the OpenAPI spec
     * is updated to include it.
     */
    fun toDomain(dto: ReviewStatsResponse, providerId: String): ReviewStats =
        ReviewStats(
            providerId = providerId,
            averageRating = dto.averageRating,
            totalReviews = dto.totalCount,
            ratingDistribution = emptyMap(),
        )

    fun toDomain(dto: CanReviewResponse): Boolean = dto.canReview

    fun toDomain(dtos: List<ReviewResponse>): List<Review> = dtos.map { toDomain(it) }
}
