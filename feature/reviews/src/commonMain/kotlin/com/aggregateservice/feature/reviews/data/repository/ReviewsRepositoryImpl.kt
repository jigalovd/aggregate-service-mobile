package com.aggregateservice.feature.reviews.data.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.reviews.data.api.ReviewsApiService
import com.aggregateservice.feature.reviews.data.dto.CreateReviewRequest
import com.aggregateservice.feature.reviews.data.mapper.ReviewMapper
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository

/**
 * Implementation of ReviewsRepository.
 *
 * Uses explicit fold() instead of mapCatching() for better error handling:
 * - Preserves AppError types from API layer
 * - Wraps unexpected exceptions in AppError.UnknownError
 * - Provides clear stack traces for debugging
 */
class ReviewsRepositoryImpl(
    private val apiService: ReviewsApiService,
) : ReviewsRepository {

    override suspend fun getProviderReviews(
        providerId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<Review>> {
        return apiService.getProviderReviews(providerId, page, pageSize).fold(
            onSuccess = { dtos -> Result.success(ReviewMapper.toDomain(dtos)) },
            onFailure = { error -> Result.failure(error.toAppError()) },
        )
    }

    override suspend fun getReviewStats(providerId: String): Result<ReviewStats> {
        return apiService.getReviewStats(providerId).fold(
            onSuccess = { dto -> Result.success(ReviewMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error.toAppError()) },
        )
    }

    override suspend fun canReviewBooking(bookingId: String): Result<Boolean> {
        return apiService.canReviewBooking(bookingId).fold(
            onSuccess = { dto -> Result.success(ReviewMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error.toAppError()) },
        )
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
        return apiService.createReview(request).fold(
            onSuccess = { dto -> Result.success(ReviewMapper.toDomain(dto)) },
            onFailure = { error -> Result.failure(error.toAppError()) },
        )
    }
}
