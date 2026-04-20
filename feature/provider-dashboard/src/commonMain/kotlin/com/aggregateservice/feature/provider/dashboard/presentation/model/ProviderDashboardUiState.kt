package com.aggregateservice.feature.provider.dashboard.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats

/**
 * UI State для экрана дашборда провайдера.
 *
 * **UDF Pattern:** Immutable state, обновляется через ScreenModel.
 *
 * Использует sealed class для строгой типизации состояний:
 * - Loading: начальная загрузка данных
 * - Content: успешно загруженные данные
 * - Error: ошибка при загрузке
 *
 * @property todaysBookings Список бронирований на сегодня
 * @property earningsSummary Summary заработков
 * @property providerStats Статистика провайдера
 * @property isRefreshing Обновление (pull-to-refresh)
 */
@Stable
sealed class ProviderDashboardUiState {
    /**
     * Загрузка данных.
     */
    data object Loading : ProviderDashboardUiState()

    /**
     * Данные успешно загружены.
     *
     * @property todaysBookings Список бронирований на сегодня
     * @property earningsSummary Summary заработков
     * @property providerStats Статистика провайдера
     * @property isRefreshing Флаг обновления
     */
    data class Content(
        val todaysBookings: List<DashboardBooking> = emptyList(),
        val earningsSummary: EarningsSummary = EarningsSummary.empty(),
        val providerStats: ProviderStats = ProviderStats.empty(),
        val isRefreshing: Boolean = false,
    ) : ProviderDashboardUiState() {
        /**
         * Пустой список бронирований (нет данных, не ошибка).
         */
        val isEmpty: Boolean
            get() = todaysBookings.isEmpty()
    }

    /**
     * Ошибка при загрузке данных.
     *
     * @property error Тип ошибки
     * @property todaysBookings Кешированные данные (опционально)
     * @property earningsSummary Кешированные данные (опционально)
     * @property providerStats Кешированные данные (опционально)
     */
    data class Error(
        val error: AppError,
        val todaysBookings: List<DashboardBooking> = emptyList(),
        val earningsSummary: EarningsSummary = EarningsSummary.empty(),
        val providerStats: ProviderStats = ProviderStats.empty(),
    ) : ProviderDashboardUiState() {
        /**
         * Есть ли кешированные данные для отображения.
         */
        val hasCachedData: Boolean
            get() = todaysBookings.isNotEmpty() ||
                earningsSummary.todayAmount != 0.0 ||
                providerStats.totalBookings != 0
    }

    companion object {
        /**
         * Начальное состояние (загрузка).
         */
        val Loading = ProviderDashboardUiState.Loading

        /**
         * Помощник для создания состояния Content.
         */
        fun content(
            todaysBookings: List<DashboardBooking> = emptyList(),
            earningsSummary: EarningsSummary = EarningsSummary.empty(),
            providerStats: ProviderStats = ProviderStats.empty(),
        ): Content = Content(todaysBookings, earningsSummary, providerStats)

        /**
         * Помощник для создания состояния Error.
         */
        fun error(
            error: AppError,
            todaysBookings: List<DashboardBooking> = emptyList(),
            earningsSummary: EarningsSummary = EarningsSummary.empty(),
            providerStats: ProviderStats = ProviderStats.empty(),
        ): Error = Error(error, todaysBookings, earningsSummary, providerStats)
    }
}