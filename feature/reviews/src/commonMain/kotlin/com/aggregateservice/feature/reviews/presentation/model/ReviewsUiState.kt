package com.aggregateservice.feature.reviews.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats

/**
 * UI state for the Reviews screen.
 */
@Stable
data class ReviewsUiState(
    val providerId: String = "",
    val reviews: List<Review> = emptyList(),
    val stats: ReviewStats? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
) {
    val isEmpty: Boolean
        get() = !isLoading && reviews.isEmpty() && error == null

    val hasReviews: Boolean
        get() = reviews.isNotEmpty()

    val canLoadMore: Boolean
        get() = !isLoading && !isLoadingMore && hasMore

    companion object {
        val Loading = ReviewsUiState(isLoading = true)
    }
}

/**
 * UI state for the Write Review dialog.
 */
@Stable
data class WriteReviewUiState(
    val bookingId: String = "",
    val providerName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val isChecking: Boolean = true,
    val canReview: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
) {
    val isValid: Boolean
        get() = rating in 1..5

    val hasComment: Boolean
        get() = comment.isNotBlank()

    companion object {
        val Checking = WriteReviewUiState(isChecking = true)
    }
}
