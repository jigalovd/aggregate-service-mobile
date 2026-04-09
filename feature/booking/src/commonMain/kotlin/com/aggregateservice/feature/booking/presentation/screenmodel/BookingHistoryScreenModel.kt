package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.domain.usecase.CancelBookingUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetClientBookingsUseCase
import com.aggregateservice.feature.booking.presentation.model.BookingHistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingHistoryScreenModel(
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow(BookingHistoryUiState.Loading)
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()

    private var currentPage: Int = 1
    private val pageSize: Int = GetClientBookingsUseCase.DEFAULT_PAGE_SIZE
    var hasMore: Boolean = true
        private set

    fun loadBookings() {
        currentPage = 1
        hasMore = true

        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getClientBookingsUseCase(
                status = _uiState.value.selectedStatus?.name,
                page = currentPage,
                pageSize = pageSize,
            ).fold(
                onSuccess = { bookings ->
                    hasMore = bookings.size >= pageSize
                    _uiState.update {
                        BookingHistoryUiState(
                            bookings = bookings,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { error ->
                    val appError =
                        error as? AppError
                            ?: AppError.UnknownError(throwable = error, message = error.message)
                    logger.w(appError) { "Failed to load bookings: ${appError::class.simpleName}" }
                    _uiState.update {
                        BookingHistoryUiState.error(appError)
                    }
                },
            )
        }
    }

    fun loadMore() {
        if (!hasMore || _uiState.value.isLoading) return
        currentPage++

        screenModelScope.launch {
            getClientBookingsUseCase(
                status = _uiState.value.selectedStatus?.name,
                page = currentPage,
                pageSize = pageSize,
            ).fold(
                onSuccess = { newBookings ->
                    hasMore = newBookings.size >= pageSize
                    _uiState.update { state ->
                        state.copy(bookings = state.bookings + newBookings)
                    }
                },
                onFailure = { error ->
                    currentPage--
                    val appError =
                        error as? AppError
                            ?: AppError.UnknownError(throwable = error, message = error.message)
                    logger.w(appError) { "Failed to load more bookings" }
                },
            )
        }
    }

    fun refresh() {
        screenModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            currentPage = 1
            hasMore = true

            getClientBookingsUseCase(
                status = _uiState.value.selectedStatus?.name,
                page = 1,
                pageSize = pageSize,
            ).fold(
                onSuccess = { bookings ->
                    hasMore = bookings.size >= pageSize
                    _uiState.update {
                        it.copy(
                            bookings = bookings,
                            isRefreshing = false,
                            isLoading = false,
                            error = null,
                        )
                    }
                },
                onFailure = { error ->
                    val appError =
                        error as? AppError
                            ?: AppError.UnknownError(throwable = error, message = error.message)
                    _uiState.update { it.copy(isRefreshing = false, error = appError) }
                },
            )
        }
    }

    fun filterByStatus(status: BookingStatus?) {
        _uiState.update { state ->
            state.copy(selectedStatus = status)
        }
        loadBookings()
    }

    fun cancelBooking(bookingId: String, reason: String? = null) {
        screenModelScope.launch {
            cancelBookingUseCase(bookingId, reason).fold(
                onSuccess = { updatedBooking ->
                    _uiState.update { state ->
                        val updatedList =
                            state.bookings.map { booking ->
                                if (booking.id == bookingId) updatedBooking else booking
                            }
                        state.copy(bookings = updatedList)
                    }
                },
                onFailure = { error ->
                    val appError =
                        error as? AppError
                            ?: AppError.UnknownError(throwable = error, message = error.message)
                    logger.w(appError) { "Failed to cancel booking: ${appError::class.simpleName}" }
                    _uiState.update {
                        it.copy(error = appError)
                    }
                },
            )
        }
    }

    fun retry() {
        loadBookings()
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(error = null)
        }
    }
}
