package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.feature.booking.domain.usecase.CreateBookingUseCase
import com.aggregateservice.feature.booking.presentation.model.BookingConfirmationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана подтверждения бронирования.
 *
 * **Responsibilities:**
 * - Управление данными для подтверждения
 * - Отправка бронирования
 * - Обработка успеха/ошибки
 *
 * @property createBookingUseCase UseCase для создания бронирования
 */
class BookingConfirmationScreenModel(
    private val createBookingUseCase: CreateBookingUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(BookingConfirmationUiState.Initial)
    val uiState: StateFlow<BookingConfirmationUiState> = _uiState.asStateFlow()

    /**
     * Инициализирует состояние с данными из предыдущих экранов.
     *
     * @param providerId ID мастера
     * @param providerName Название мастера
     * @param services Выбранные услуги
     * @param selectedDate Выбранная дата
     * @param selectedSlot Выбранный слот
     */
    fun initialize(
        providerId: String,
        providerName: String,
        services: List<com.aggregateservice.feature.booking.domain.model.BookingService>,
        selectedDate: kotlinx.datetime.LocalDate?,
        selectedSlot: com.aggregateservice.feature.booking.domain.model.TimeSlot?,
    ) {
        _uiState.update {
            BookingConfirmationUiState(
                providerId = providerId,
                providerName = providerName,
                services = services,
                selectedDate = selectedDate,
                selectedSlot = selectedSlot,
            )
        }
    }

    /**
     * Обновляет заметки клиента.
     *
     * @param notes Новые заметки
     */
    fun updateNotes(notes: String) {
        _uiState.update { state ->
            state.copy(notes = notes)
        }
    }

    /**
     * Отправляет бронирование.
     */
    fun submitBooking() {
        val currentState = _uiState.value

        if (!currentState.canSubmit) return

        val slot = currentState.selectedSlot ?: return

        screenModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val serviceIds = currentState.services.map { it.id }

            createBookingUseCase(
                providerId = currentState.providerId,
                serviceIds = serviceIds,
                startTime = slot.startTime,
                notes = currentState.notes.takeIf { it.isNotBlank() },
            ).fold(
                onSuccess = { booking ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isBooked = true,
                            booking = booking,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error as? com.aggregateservice.core.network.AppError
                                ?: com.aggregateservice.core.network.AppError.UnknownError(throwable = error, message = error.message),
                        )
                    }
                },
            )
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
