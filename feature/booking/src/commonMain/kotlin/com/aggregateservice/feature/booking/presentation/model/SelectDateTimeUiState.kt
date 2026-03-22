package com.aggregateservice.feature.booking.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

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
 */
@Stable
data class SelectDateTimeUiState(
    val selectedDate: LocalDate? = null,
    val availableSlots: List<TimeSlot> = emptyList(),
    val selectedSlot: TimeSlot? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val currentMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
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
        get() = availableSlots.map { slot ->
            slot.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.toSet()

    /**
     * Доступные слоты для выбранной даты.
     */
    val slotsForSelectedDate: List<TimeSlot>
        get() = if (selectedDate == null) {
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
        val Initial = SelectDateTimeUiState()
    }
}
