package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.domain.usecase.CancelBookingUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetAvailableSlotsUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetBookingByIdUseCase
import com.aggregateservice.feature.booking.domain.usecase.RescheduleBookingUseCase
import com.aggregateservice.feature.booking.presentation.model.BookingDetailUiState
import com.aggregateservice.feature.booking.presentation.model.RescheduleSheetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class BookingDetailScreenModel(
    private val getBookingByIdUseCase: GetBookingByIdUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val rescheduleBookingUseCase: RescheduleBookingUseCase,
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private val _rescheduleState = MutableStateFlow(RescheduleSheetState())
    val rescheduleState: StateFlow<RescheduleSheetState> = _rescheduleState.asStateFlow()

    fun loadBooking(bookingId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        screenModelScope.launch {
            getBookingByIdUseCase(bookingId).fold(
                onSuccess = { booking ->
                    _uiState.update { it.copy(booking = booking, isLoading = false, error = null) }
                },
                onFailure = { error ->
                    logger.w(error) { "Failed to load booking $bookingId" }
                    _uiState.update { it.copy(isLoading = false, error = error.toAppError()) }
                },
            )
        }
    }

    fun cancelBooking(reason: String = "") {
        val booking = _uiState.value.booking ?: return
        _uiState.update { it.copy(isCancelling = true, cancelError = null) }

        screenModelScope.launch {
            cancelBookingUseCase(booking.id, reason.ifBlank { null }).fold(
                onSuccess = { updatedBooking ->
                    _uiState.update {
                        it.copy(booking = updatedBooking, isCancelling = false)
                    }
                },
                onFailure = { error ->
                    logger.w(error) { "Cancel failed for booking ${booking.id}" }
                    _uiState.update { it.copy(isCancelling = false, cancelError = error.toAppError()) }
                },
            )
        }
    }

    fun loadSlots(providerId: String, date: LocalDate, serviceIds: List<String>) {
        _rescheduleState.update { it.copy(isLoading = true, error = null, selectedSlot = null) }

        screenModelScope.launch {
            getAvailableSlotsUseCase(providerId, date, date, serviceIds).fold(
                onSuccess = { slots ->
                    _rescheduleState.update { it.copy(slots = slots, isLoading = false, selectedDate = date) }
                },
                onFailure = { error ->
                    logger.w(error) { "Failed to load slots for $providerId on $date" }
                    _rescheduleState.update { it.copy(isLoading = false, error = error.toAppError()) }
                },
            )
        }
    }

    fun selectSlot(slot: TimeSlot) {
        _rescheduleState.update { it.copy(selectedSlot = slot) }
    }

    fun submitReschedule(bookingId: String, reason: String = "") {
        val slot = _rescheduleState.value.selectedSlot ?: return
        _rescheduleState.update { it.copy(isLoading = true) }

        screenModelScope.launch {
            rescheduleBookingUseCase(bookingId, slot.startTime).fold(
                onSuccess = { updatedBooking ->
                    _uiState.update { it.copy(booking = updatedBooking) }
                    _rescheduleState.update {
                        RescheduleSheetState()
                    }
                },
                onFailure = { error ->
                    logger.w(error) { "Reschedule failed for booking $bookingId" }
                    _rescheduleState.update { it.copy(isLoading = false, error = error.toAppError()) }
                },
            )
        }
    }

    fun retry() {
        val booking = _uiState.value.booking
        if (booking != null) {
            loadBooking(booking.id)
        }
    }

    fun clearCancelError() {
        _uiState.update { it.copy(cancelError = null) }
    }
}
