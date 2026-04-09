package com.aggregateservice.feature.booking.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import kotlinx.datetime.LocalDate

/**
 * UI State для экрана подтверждения бронирования.
 *
 * **UDF Pattern:** Immutable state, обновляется через ScreenModel.
 *
 * @property providerId ID мастера
 * @property providerName Название бизнеса мастера
 * @property services Выбранные услуги
 * @property selectedDate Выбранная дата
 * @property selectedSlot Выбранный временной слот
 * @property notes Заметки клиента
 * @property isSubmitting Отправка бронирования
 * @property isBooked Бронирование успешно создано
 * @property booking Созданное бронирование
 * @property error Ошибка
 */
@Stable
data class BookingConfirmationUiState(
    val providerId: String = "",
    val providerName: String = "",
    val services: List<BookingService> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedSlot: TimeSlot? = null,
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val isBooked: Boolean = false,
    val booking: Booking? = null,
    val error: AppError? = null,
) {
    /**
     * Общая стоимость услуг.
     */
    val totalPrice: Double
        get() = services.sumOf { it.price }

    /**
     * Общая длительность услуг.
     */
    val totalDurationMinutes: Int
        get() = services.sumOf { it.durationMinutes }

    /**
     * Можно ли отправить бронирование.
     */
    val canSubmit: Boolean
        get() = services.isNotEmpty() && selectedSlot != null && !isSubmitting && !isBooked

    /**
     * Форматированная общая стоимость.
     */
    val formattedTotal: String
        get() =
            if (services.isNotEmpty()) {
                val currency = services.first().currency
                "%.0f %s".format(totalPrice, currency)
            } else {
                "0 ILS"
            }

    /**
     * Форматированная длительность.
     */
    val formattedDuration: String
        get() = "$totalDurationMinutes min"

    companion object {
        /**
         * Начальное состояние.
         */
        val Initial = BookingConfirmationUiState()
    }
}
