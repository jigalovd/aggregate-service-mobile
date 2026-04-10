@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
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

class SelectDateTimeScreenModel(
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow(SelectDateTimeUiState.Initial)
    val uiState: StateFlow<SelectDateTimeUiState> = _uiState.asStateFlow()

    fun loadAvailableSlots(providerId: String, serviceIds: List<String>) {
        if (_uiState.value.isLoading) {
            return
        }

        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val horizonDays = SelectDateTimeUiState.BOOKING_HORIZON_DAYS - 1
                val endDate = today.plus(horizonDays, DateTimeUnit.DAY)

                getAvailableSlotsUseCase(providerId, today, endDate, serviceIds).fold(
                    onSuccess = { slots ->
                        _uiState.update {
                            SelectDateTimeUiState(
                                availableSlots = slots,
                                isLoading = false,
                            )
                        }
                    },
                    onFailure = { error ->
                        val appError = (error as? AppError) ?: error.toAppError()
                        logger.w(appError) { "Failed to load slots" }
                        _uiState.update {
                            it.copy(isLoading = false, error = appError)
                        }
                    },
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                throw e
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val maxDate = today.plus(SelectDateTimeUiState.BOOKING_HORIZON_DAYS, DateTimeUnit.DAY)
        val isBeyondHorizon = date > maxDate

        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                selectedSlot = null,
                bookingHorizonVisible = isBeyondHorizon,
            )
        }
    }

    fun selectSlot(slot: com.aggregateservice.feature.booking.domain.model.TimeSlot) {
        _uiState.update { state ->
            state.copy(selectedSlot = slot)
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(error = null)
        }
    }
}
