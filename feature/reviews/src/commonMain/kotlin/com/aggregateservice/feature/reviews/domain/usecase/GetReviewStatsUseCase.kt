package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.feature.reviews.domain.model.ReviewStats
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository

/**
 * Use case for fetching review statistics for a provider.
 */
class GetReviewStatsUseCase(
    private val repository: ReviewsRepository,
) {
    suspend operator fun invoke(providerId: String): Result<ReviewStats> {
        if (providerId.isBlank()) {
            return Result.failure(IllegalArgumentException("Provider ID cannot be empty"))
        }

        return repository.getReviewStats(providerId)
    }
}
