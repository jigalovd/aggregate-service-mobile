package com.aggregateservice.feature.booking.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.BookingService

/**
 * UI State для экрана выбора услуг.
 *
 * **UDF Pattern:** Immutable state, обновляется через ScreenModel.
 *
 * @property services Список доступных услуг мастера
 * @property selectedServices Выбранные пользователем услуги
 * @property isLoading Загрузка услуг
 * @property error Ошибка загрузки
 */
@Stable
data class SelectServiceUiState(
    val services: List<BookingService> = emptyList(),
    val selectedServices: List<BookingService> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val nonCombinableError: String? = null,
) {
    /**
     * Общая стоимость выбранных услуг.
     */
    val totalPrice: Double
        get() = selectedServices.sumOf { it.price }

    /**
     * Общая длительность выбранных услуг в минутах.
     */
    val totalDurationMinutes: Int
        get() = selectedServices.sumOf { it.durationMinutes }

    /**
     * Есть ли выбранные услуги.
     */
    val hasSelection: Boolean
        get() = selectedServices.isNotEmpty()

    /**
     * Форматированная общая стоимость (например, "300 ILS").
     */
    val formattedTotal: String
        get() =
            if (hasSelection) {
                val currency = selectedServices.firstOrNull()?.currency ?: "ILS"
                "%.0f %s".format(totalPrice, currency)
            } else {
                "0 ILS"
            }

    /**
     * Форматированная общая длительность (например, "90 min").
     */
    val formattedDuration: String
        get() = "$totalDurationMinutes min"

    /**
     * Проверка, выбрана ли услуга.
     */
    fun isSelected(service: BookingService): Boolean = selectedServices.any { it.id == service.id }

    companion object {
        /**
         * Начальное состояние (загрузка).
         */
        val Loading = SelectServiceUiState(isLoading = true)

        /**
         * Состояние ошибки.
         */
        fun error(error: AppError) =
            SelectServiceUiState(
                isLoading = false,
                error = error,
            )
    }
}
