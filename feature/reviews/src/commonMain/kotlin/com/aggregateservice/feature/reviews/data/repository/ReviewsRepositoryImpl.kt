package com.aggregateservice.feature.reviews.data.repository

import com.aggregateservice.feature.reviews.data.api.ReviewsApiService
import com.aggregateservice.feature.reviews.data.dto.CreateReviewRequest
import com.aggregateservice.feature.reviews.data.mapper.ReviewMapper
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository

/**
 * Implementation of ReviewsRepository.
 */
class ReviewsRepositoryImpl(
    private val apiService: ReviewsApiService,
) : ReviewsRepository {

    override suspend fun getProviderReviews(
        providerId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<Review>> {
        return apiService.getProviderReviews(providerId, page, pageSize)
            .mapCatching { dtos -> ReviewMapper.toDomain(dtos) }
    }

    override suspend fun getReviewStats(providerId: String): Result<ReviewStats> {
        return apiService.getReviewStats(providerId)
            .mapCatching { dto -> ReviewMapper.toDomain(dto) }
    }

    override suspend fun canReviewBooking(bookingId: String): Result<Boolean> {
        return apiService.canReviewBooking(bookingId)
            .mapCatching { dto -> ReviewMapper.toDomain(dto) }
    }

    override suspend fun createReview(
        bookingId: String,
        rating: Int,
        comment: String?,
    ): Result<Review> {
        val request = CreateReviewRequest(
            bookingId = bookingId,
            rating = rating,
            comment = comment,
        )
        return apiService.createReview(request)
            .mapCatching { dto -> ReviewMapper.toDomain(dto) }
    }
}
