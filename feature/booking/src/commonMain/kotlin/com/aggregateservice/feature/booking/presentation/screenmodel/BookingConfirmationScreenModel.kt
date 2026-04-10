package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.usecase.CreateBookingUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetAvailableSlotsUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetBookingServicesUseCase
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
 * @property getBookingServicesUseCase UseCase для получения услуг
 */
class BookingConfirmationScreenModel(
    private val createBookingUseCase: CreateBookingUseCase,
    private val getBookingServicesUseCase: GetBookingServicesUseCase,
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow(BookingConfirmationUiState.Initial)
    val uiState: StateFlow<BookingConfirmationUiState> = _uiState.asStateFlow()

    /**
     * Загружает услуги по их IDs.
     *
     * @param providerId ID провайдера
     * @param serviceIds Список ID услуг
     */
    fun loadServices(providerId: String, serviceIds: List<String>) {
        if (serviceIds.isEmpty()) return

        screenModelScope.launch {
            getBookingServicesUseCase(providerId).fold(
                onSuccess = { allServices ->
                    val filteredServices = allServices.filter { it.id in serviceIds }
                    _uiState.update { it.copy(services = filteredServices) }
                },
                onFailure = { error ->
                    val appError = (error as? AppError) ?: error.toAppError()
                    logger.w(appError) { "Failed to load booking services, continuing" }
                    _uiState.update { it.copy(services = emptyList()) }
                },
            )
        }
    }

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
        services: List<BookingService>,
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
        val date = currentState.selectedDate ?: return
        if (currentState.services.isEmpty()) return

        screenModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val serviceIds = currentState.services.map { it.id }

            // D-04: Auto-refresh slots before confirmation
            val slotsResult =
                getAvailableSlotsUseCase(
                    providerId = currentState.providerId,
                    fromDate = date,
                    toDate = date,
                    serviceIds = serviceIds,
                )

            val isSlotStillAvailable =
                slotsResult.fold(
                    onSuccess = { availableSlots ->
                        availableSlots.any { it.startTime == slot.startTime && it.isAvailable }
                    },
                    onFailure = { error ->
                        val appError = (error as? AppError) ?: error.toAppError()
                        logger.w(appError) { "Failed to verify slot availability, treating as unavailable" }
                        false
                    },
                )

            if (!isSlotStillAvailable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error =
                            AppError.FormValidation(
                                field = "slot",
                                rule = ValidationRule.InvalidValue,
                            ),
                    )
                }
                return@launch
            }

            // Slot verified, proceed with booking
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
                    val appError =
                        error as? AppError
                            ?: AppError.UnknownError(throwable = error, message = error.message)
                    logger.w(appError) { "Booking creation failed: ${appError::class.simpleName}" }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = appError,
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
