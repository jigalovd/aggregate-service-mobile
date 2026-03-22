package com.aggregateservice.feature.reviews.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.reviews.domain.usecase.CanReviewBookingUseCase
import com.aggregateservice.feature.reviews.domain.usecase.CreateReviewUseCase
import com.aggregateservice.feature.reviews.domain.usecase.GetProviderReviewsUseCase
import com.aggregateservice.feature.reviews.domain.usecase.GetReviewStatsUseCase
import com.aggregateservice.feature.reviews.presentation.model.ReviewsUiState
import com.aggregateservice.feature.reviews.presentation.model.WriteReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Reviews screen.
 */
class ReviewsScreenModel(
    private val getProviderReviewsUseCase: GetProviderReviewsUseCase,
    private val getReviewStatsUseCase: GetReviewStatsUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    private var currentProviderId: String = ""

    fun initialize(providerId: String) {
        if (currentProviderId == providerId && _uiState.value.hasReviews) {
            return // Already loaded
        }
        currentProviderId = providerId
        _uiState.update { it.copy(providerId = providerId) }
        loadReviews(providerId)
    }

    fun loadReviews(providerId: String = currentProviderId) {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load stats and reviews in parallel
            val statsResult = getReviewStatsUseCase(providerId)
            val reviewsResult = getProviderReviewsUseCase(
                providerId = providerId,
                page = GetProviderReviewsUseCase.DEFAULT_PAGE,
                pageSize = GetProviderReviewsUseCase.DEFAULT_PAGE_SIZE,
            )

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    stats = statsResult.getOrNull(),
                    reviews = reviewsResult.getOrNull() ?: emptyList(),
                    error = reviewsResult.exceptionOrNull()?.let {
                        AppError.UnknownError(it)
                    },
                    hasMore = (reviewsResult.getOrNull()?.size ?: 0) >= GetProviderReviewsUseCase.DEFAULT_PAGE_SIZE,
                    currentPage = 1,
                )
            }
        }
    }

    fun refresh() {
        screenModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadReviews(currentProviderId)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore) return

        screenModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            val result = getProviderReviewsUseCase(
                providerId = currentProviderId,
                page = nextPage,
                pageSize = GetProviderReviewsUseCase.DEFAULT_PAGE_SIZE,
            )

            result.fold(
                onSuccess = { newReviews ->
                    _uiState.update { state ->
                        state.copy(
                            reviews = state.reviews + newReviews,
                            isLoadingMore = false,
                            currentPage = nextPage,
                            hasMore = newReviews.size >= GetProviderReviewsUseCase.DEFAULT_PAGE_SIZE,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingMore = false,
                            error = AppError.UnknownError(error),
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * ScreenModel for writing a review.
 */
class WriteReviewScreenModel(
    private val canReviewBookingUseCase: CanReviewBookingUseCase,
    private val createReviewUseCase: CreateReviewUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(WriteReviewUiState.Checking)
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    fun initialize(bookingId: String, providerName: String) {
        screenModelScope.launch {
            _uiState.update {
                WriteReviewUiState(
                    bookingId = bookingId,
                    providerName = providerName,
                    isChecking = true,
                )
            }

            canReviewBookingUseCase(bookingId).fold(
                onSuccess = { canReview ->
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            canReview = canReview,
                            error = if (!canReview) "Вы уже оставили отзыв для этого бронирования" else null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            canReview = false,
                            error = error.message ?: "Не удалось проверить возможность отзыва",
                        )
                    }
                },
            )
        }
    }

    fun setRating(rating: Int) {
        _uiState.update { it.copy(rating = rating, error = null) }
    }

    fun setComment(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun submitReview() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(error = "Пожалуйста, выберите рейтинг") }
            return
        }

        screenModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            createReviewUseCase(
                bookingId = state.bookingId,
                rating = state.rating,
                comment = state.comment.takeIf { it.isNotBlank() },
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Не удалось отправить отзыв",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
