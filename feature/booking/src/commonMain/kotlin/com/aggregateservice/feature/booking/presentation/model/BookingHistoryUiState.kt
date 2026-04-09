package com.aggregateservice.feature.booking.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingStatus

/**
 * UI State для экрана истории бронирований.
 *
 * **UDF Pattern:** Immutable state, обновляется через ScreenModel.
 *
 * @property bookings Список бронирований
 * @property isLoading Загрузка
 * @property error Ошибка
 * @property selectedStatus Фильтр по статусу
 * @property isRefreshing Обновление (pull-to-refresh)
 */
@Stable
data class BookingHistoryUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val selectedStatus: BookingStatus? = null,
    val isRefreshing: Boolean = false,
) {
    /**
     * Предстоящие бронирования (PENDING, CONFIRMED).
     */
    val upcomingBookings: List<Booking>
        get() = bookings.filter { it.status.isActive }

    /**
     * Прошедшие бронирования (COMPLETED, CANCELLED, etc.).
     */
    val pastBookings: List<Booking>
        get() = bookings.filter { it.status.isPast }

    /**
     * Отфильтрованный список по статусу.
     */
    val filteredBookings: List<Booking>
        get() =
            if (selectedStatus != null) {
                bookings.filter { it.status == selectedStatus }
            } else {
                bookings
            }

    /**
     * Пустой список.
     */
    val isEmpty: Boolean
        get() = !isLoading && bookings.isEmpty() && error == null

    companion object {
        /**
         * Начальное состояние (загрузка).
         */
        val Loading = BookingHistoryUiState(isLoading = true)

        /**
         * Состояние ошибки.
         */
        fun error(error: AppError) =
            BookingHistoryUiState(
                isLoading = false,
                error = error,
            )
    }
}
