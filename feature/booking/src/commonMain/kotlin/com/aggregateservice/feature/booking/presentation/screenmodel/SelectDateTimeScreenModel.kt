@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.feature.booking.domain.usecase.GetAvailableSlotsUseCase
import com.aggregateservice.feature.booking.presentation.model.SelectDateTimeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * ScreenModel для экрана выбора даты и времени.
 *
 * **Responsibilities:**
 * - Загрузка доступных слотов для мастера
 * - Управление выбором даты
 * - Управление выбором временного слота
 *
 * @property getAvailableSlotsUseCase UseCase для загрузки слотов
 */
class SelectDateTimeScreenModel(
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(SelectDateTimeUiState.Initial)
    val uiState: StateFlow<SelectDateTimeUiState> = _uiState.asStateFlow()

    /**
     * Загружает доступные слоты для мастера на определённую дату.
     *
     * @param providerId ID мастера
     * @param serviceIds Список ID услуг (для вычисления длительности)
     */
    fun loadAvailableSlots(providerId: String, serviceIds: List<String>) {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Загружаем слоты на ближайшие 7 дней
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val allSlots = mutableListOf<com.aggregateservice.feature.booking.domain.model.TimeSlot>()

            for (i in 0..6) {
                val date = today.plus(i, DateTimeUnit.DAY)
                getAvailableSlotsUseCase(providerId, date, serviceIds).fold(
                    onSuccess = { slots -> allSlots.addAll(slots) },
                    onFailure = { /* Continue loading other days */ },
                )
            }

            _uiState.update {
                SelectDateTimeUiState(
                    availableSlots = allSlots,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Выбирает дату.
     *
     * @param date Выбранная дата
     */
    fun selectDate(date: LocalDate) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val maxDate = today.plus(SelectDateTimeUiState.BOOKING_HORIZON_DAYS, DateTimeUnit.DAY)
        val isBeyondHorizon = date > maxDate

        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                selectedSlot = null, // Reset slot when date changes
                bookingHorizonVisible = isBeyondHorizon,
            )
        }
    }

    /**
     * Выбирает временной слот.
     *
     * @param slot Выбранный слот
     */
    fun selectSlot(slot: com.aggregateservice.feature.booking.domain.model.TimeSlot) {
        _uiState.update { state ->
            state.copy(selectedSlot = slot)
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
