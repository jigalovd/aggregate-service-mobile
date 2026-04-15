@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * UI State для экрана выбора даты и времени.
 *
 * **UDF Pattern:** Immutable state, обновляется через ScreenModel.
 *
 * @property selectedDate Выбранная дата
 * @property availableSlots Доступные слоты для выбранной даты
 * @property selectedSlot Выбранный временной слот
 * @property isLoading Загрузка слотов
 * @property error Ошибка загрузки
 * @property currentMonth Текущий месяц для календаря
 * @property maxAdvanceDays Максимальное количество дней для бронирования вперед (US-3.34)
 * @property bookingHorizonVisible Видимость подсказки о горизонте бронирования
 */
@Stable
data class SelectDateTimeUiState(
    val selectedDate: LocalDate? = null,
    val availableSlots: List<TimeSlot> = emptyList(),
    val selectedSlot: TimeSlot? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val currentMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val maxAdvanceDays: Int = BOOKING_HORIZON_DAYS,
    val bookingHorizonVisible: Boolean = false,
) {
    /**
     * Есть ли полный выбор (дата + слот).
     */
    val hasSelection: Boolean
        get() = selectedDate != null && selectedSlot != null

    /**
     * Доступные даты (на основе слотов).
     */
    val availableDates: Set<LocalDate>
        get() =
            availableSlots
                .map { slot ->
                    slot.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
                }.toSet()

    /**
     * Доступные слоты для выбранной даты.
     */
    val slotsForSelectedDate: List<TimeSlot>
        get() =
            if (selectedDate == null) {
                emptyList()
            } else {
                availableSlots.filter { slot ->
                    slot.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date == selectedDate
                }
            }

    /**
     * Только доступные (не занятые) слоты.
     */
    val availableSlotsForDate: List<TimeSlot>
        get() = slotsForSelectedDate.filter { it.isAvailable }

    companion object {
        /**
         * Начальное состояние.
         */
        val Initial = SelectDateTimeUiState(isLoading = true)

        /**
         * Максимальное количество дней для бронирования вперед.
         * US-3.34: Мастер хочет ограничить бронирование 30 днями вперед.
         */
        const val BOOKING_HORIZON_DAYS = 30
    }
}
