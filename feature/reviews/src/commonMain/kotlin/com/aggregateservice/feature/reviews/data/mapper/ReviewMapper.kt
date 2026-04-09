package com.aggregateservice.feature.reviews.data.mapper

import com.aggregateservice.feature.reviews.data.dto.CanReviewResponseDto
import com.aggregateservice.feature.reviews.data.dto.ProviderReplyDto
import com.aggregateservice.feature.reviews.data.dto.ReviewDto
import com.aggregateservice.feature.reviews.data.dto.ReviewStatsDto
import com.aggregateservice.feature.reviews.domain.model.ProviderReply
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats

/**
 * Mapper for converting DTOs to Domain models.
 */
object ReviewMapper {
    fun toDomain(dto: ReviewDto): Review =
        Review(
            id = dto.id,
            bookingId = dto.bookingId,
            providerId = dto.providerId,
            providerName = dto.providerName,
            clientId = dto.clientId,
            clientName = dto.clientName,
            rating = dto.rating,
            comment = dto.comment,
            providerReply = dto.providerReply?.toDomain(),
            createdAt = dto.createdAt,
        )

    fun toDomain(dto: ReviewStatsDto): ReviewStats =
        ReviewStats(
            providerId = dto.providerId,
            averageRating = dto.averageRating,
            totalReviews = dto.totalReviews,
            ratingDistribution =
                dto.ratingDistribution
                    .mapKeys { it.key.toIntOrNull() ?: 0 }
                    .filterKeys { it in 1..5 },
        )

    fun toDomain(dto: CanReviewResponseDto): Boolean = dto.canReview

    private fun ProviderReplyDto.toDomain(): ProviderReply =
        ProviderReply(
            text = text,
            createdAt = createdAt,
        )

    fun toDomain(dtos: List<ReviewDto>): List<Review> = dtos.map { toDomain(it) }
}
