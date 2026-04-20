package com.aggregateservice.feature.provider.bookings.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.bookings.domain.model.BookingFilter
import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking

/**
 * UI State для экрана управления бронированиями провайдера.
 *
 * **UDF Pattern:** Immutable state, обновляется через ScreenModel.
 *
 * Использует sealed class для строгой типизации состояний:
 * - Loading: начальная загрузка данных
 * - Content: успешно загруженные данные
 * - Error: ошибка при загрузке
 *
 * @property bookings Список бронирований
 * @property selectedFilter Текущий фильтр статуса
 * @property isRefreshing Флаг обновления (pull-to-refresh)
 * @property isLoadingAction Флаг выполнения действия (accept/reject/cancel)
 * @property actionError Сообщение об ошибке действия (опционально)
 */
@Stable
sealed class ProviderBookingsUiState {
    /**
     * Загрузка данных.
     */
    data object Loading : ProviderBookingsUiState()

    /**
     * Данные успешно загружены.
     *
     * @property bookings Список бронирований (отфильтрованный)
     * @property selectedFilter Текущий фильтр статуса
     * @property isRefreshing Флаг обновления
     * @property isLoadingAction Флаг выполнения действия
     * @property actionError Сообщение об ошибке действия
     */
    data class Content(
        val bookings: List<ProviderBooking> = emptyList(),
        val selectedFilter: BookingFilter = BookingFilter.ALL,
        val isRefreshing: Boolean = false,
        val isLoadingAction: Boolean = false,
        val actionError: String? = null,
    ) : ProviderBookingsUiState() {
        /**
         * Пустой список бронирований (нет данных, не ошибка).
         */
        val isEmpty: Boolean
            get() = bookings.isEmpty()

        /**
         * Количество бронирований определённого статуса.
         */
        fun countByStatus(status: BookingFilter): Int {
            return bookings.count {
                status.status == null || it.status == status.status
            }
        }
    }

    /**
     * Ошибка при загрузке данных.
     *
     * @property error Тип ошибки
     * @property bookings Кешированные данные (опционально)
     * @property selectedFilter Текущий фильтр статуса
     */
    data class Error(
        val error: AppError,
        val bookings: List<ProviderBooking> = emptyList(),
        val selectedFilter: BookingFilter = BookingFilter.ALL,
    ) : ProviderBookingsUiState() {
        /**
         * Есть ли кешированные данные для отображения.
         */
        val hasCachedData: Boolean
            get() = bookings.isNotEmpty()
    }

    companion object {
        /**
         * Начальное состояние (загрузка).
         */
        val Loading = ProviderBookingsUiState.Loading

        /**
         * Помощник для создания состояния Content.
         */
        fun content(
            bookings: List<ProviderBooking> = emptyList(),
            selectedFilter: BookingFilter = BookingFilter.ALL,
        ): Content = Content(bookings, selectedFilter)

        /**
         * Помощник для создания состояния Error.
         */
        fun error(
            error: AppError,
            bookings: List<ProviderBooking> = emptyList(),
            selectedFilter: BookingFilter = BookingFilter.ALL,
        ): Error = Error(error, bookings, selectedFilter)
    }
}
