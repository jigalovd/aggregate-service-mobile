package com.aggregateservice.feature.booking.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.usecase.GetBookingServicesUseCase
import com.aggregateservice.feature.booking.presentation.model.SelectServiceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана выбора услуг.
 *
 * **Responsibilities:**
 * - Загрузка услуг мастера
 * - Управление выбором услуг (multi-select)
 * - Вычисление общей стоимости и длительности
 *
 * @property getBookingServicesUseCase UseCase для загрузки услуг
 */
class SelectServiceScreenModel(
    private val getBookingServicesUseCase: GetBookingServicesUseCase,
) : ScreenModel {
    private val _uiState = MutableStateFlow(SelectServiceUiState.Loading)
    val uiState: StateFlow<SelectServiceUiState> = _uiState.asStateFlow()

    /**
     * Загружает услуги мастера.
     *
     * @param providerId ID мастера
     */
    fun loadServices(providerId: String) {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getBookingServicesUseCase(providerId).fold(
                onSuccess = { services ->
                    _uiState.update {
                        SelectServiceUiState(
                            services = services,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        SelectServiceUiState.error(
                            error as? com.aggregateservice.core.network.AppError
                                ?: com.aggregateservice.core.network.AppError
                                    .UnknownError(throwable = error, message = error.message),
                        )
                    }
                },
            )
        }
    }

    /**
     * Переключает выбор услуги.
     * Нельзя выбрать несколько услуг, если одна из них некомбинируемая.
     *
     * @param service Услуга для выбора/отмены
     */
    fun toggleServiceSelection(service: BookingService) {
        _uiState.update { state ->
            val currentSelected = state.selectedServices.toMutableList()
            val existingIndex = currentSelected.indexOfFirst { it.id == service.id }

            if (existingIndex >= 0) {
                currentSelected.removeAt(existingIndex)
                state.copy(selectedServices = currentSelected.toList(), nonCombinableError = null)
            } else {
                val hasNonCombinableSelected = currentSelected.any { !it.isCombinable }
                if (hasNonCombinableSelected) {
                    state.copy(nonCombinableError = "Некомбинируемые услуги нельзя выбрать вместе с другими")
                } else if (!service.isCombinable && currentSelected.isNotEmpty()) {
                    state.copy(nonCombinableError = "Некомбинируемые услуги нельзя выбрать вместе с другими")
                } else {
                    currentSelected.add(service)
                    state.copy(selectedServices = currentSelected.toList(), nonCombinableError = null)
                }
            }
        }
    }

    /**
     * Очищает все выбранные услуги.
     */
    fun clearSelection() {
        _uiState.update { state ->
            state.copy(selectedServices = emptyList())
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
