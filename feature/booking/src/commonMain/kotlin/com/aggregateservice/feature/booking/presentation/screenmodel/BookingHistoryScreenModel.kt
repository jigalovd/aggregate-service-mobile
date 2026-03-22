package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.domain.usecase.CancelBookingUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetClientBookingsUseCase
import com.aggregateservice.feature.booking.presentation.model.BookingHistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана истории бронирований.
 *
 * **Responsibilities:**
 * - Загрузка истории бронирований клиента
 * - Фильтрация по статусу
 * - Отмена бронирования
 *
 * @property getClientBookingsUseCase UseCase для загрузки истории
 * @property cancelBookingUseCase UseCase для отмены бронирования
 */
class BookingHistoryScreenModel(
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(BookingHistoryUiState.Loading)
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()

    /**
     * Загружает историю бронирований.
     *
     * @param clientId ID клиента
     */
    fun loadBookings(clientId: String) {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getClientBookingsUseCase(
                clientId = clientId,
                status = _uiState.value.selectedStatus?.name,
            ).fold(
                onSuccess = { bookings ->
                    _uiState.update {
                        BookingHistoryUiState(
                            bookings = bookings,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { BookingHistoryUiState.error(error as? com.aggregateservice.core.network.AppError
                        ?: com.aggregateservice.core.network.AppError.UnknownError(throwable = error, message = error.message)) }
                },
            )
        }
    }

    /**
     * Обновляет историю (pull-to-refresh).
     *
     * @param clientId ID клиента
     */
    fun refresh(clientId: String) {
        screenModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadBookings(clientId)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Фильтрует по статусу.
     *
     * @param status Статус для фильтрации (null = все)
     */
    fun filterByStatus(status: BookingStatus?) {
        _uiState.update { state ->
            state.copy(selectedStatus = status)
        }
    }

    /**
     * Отменяет бронирование.
     *
     * @param bookingId ID бронирования
     * @param reason Причина отмены (опционально)
     */
    fun cancelBooking(bookingId: String, reason: String? = null) {
        screenModelScope.launch {
            cancelBookingUseCase(bookingId, reason).fold(
                onSuccess = { updatedBooking ->
                    _uiState.update { state ->
                        val updatedList = state.bookings.map { booking ->
                            if (booking.id == bookingId) updatedBooking else booking
                        }
                        state.copy(bookings = updatedList)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? com.aggregateservice.core.network.AppError
                                ?: com.aggregateservice.core.network.AppError.UnknownError(throwable = error, message = error.message),
                        )
                    }
                },
            )
        }
    }

    /**
     * Очищает ошибку.
     */
    fun clearError() {
        _uiState.update { state ->
            state.copy(error = null)
        }
    }
}
