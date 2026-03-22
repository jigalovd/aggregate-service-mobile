package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository

/**
 * Use case for fetching paginated reviews for a provider.
 */
class GetProviderReviewsUseCase(
    private val repository: ReviewsRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        page: Int = DEFAULT_PAGE,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<List<Review>> {
        // Validation
        if (providerId.isBlank()) {
            return Result.failure(IllegalArgumentException("Provider ID cannot be empty"))
        }
        if (page < MIN_PAGE) {
            return Result.failure(IllegalArgumentException("Page must be >= $MIN_PAGE"))
        }
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            return Result.failure(
                IllegalArgumentException("Page size must be between $MIN_PAGE_SIZE and $MAX_PAGE_SIZE"),
            )
        }

        return repository.getProviderReviews(providerId, page, pageSize)
    }

    companion object {
        const val DEFAULT_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 20
        const val MIN_PAGE = 1
        const val MIN_PAGE_SIZE = 1
        const val MAX_PAGE_SIZE = 100
    }
}
