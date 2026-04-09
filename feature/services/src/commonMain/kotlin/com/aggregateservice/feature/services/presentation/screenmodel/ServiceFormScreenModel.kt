package com.aggregateservice.feature.services.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.services.domain.usecase.CreateServiceUseCase
import com.aggregateservice.feature.services.domain.usecase.GetServiceByIdUseCase
import com.aggregateservice.feature.services.domain.usecase.UpdateServiceUseCase
import com.aggregateservice.feature.services.presentation.model.ServiceFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for service create/edit form screen.
 *
 * **Responsibilities:**
 * - Loading existing service (edit mode)
 * - Form validation
 * - Creating/updating service
 *
 * @property getServiceByIdUseCase UseCase for loading service (edit mode)
 * @property createServiceUseCase UseCase for creating service
 * @property updateServiceUseCase UseCase for updating service
 */
class ServiceFormScreenModel(
    private val getServiceByIdUseCase: GetServiceByIdUseCase,
    private val createServiceUseCase: CreateServiceUseCase,
    private val updateServiceUseCase: UpdateServiceUseCase,
    private val logger: Logger,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ServiceFormUiState.Create)
    val uiState: StateFlow<ServiceFormUiState> = _uiState.asStateFlow()

    /**
     * Loads an existing service for editing.
     *
     * @param serviceId Service ID to edit
     */
    fun loadService(serviceId: String) {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getServiceByIdUseCase(serviceId).fold(
                onSuccess = { service ->
                    _uiState.update { ServiceFormUiState.fromService(service) }
                },
                onFailure = { error ->
                    val appError = error.toAppError()
                    logger.w(appError) { "Failed to load service: ${appError::class.simpleName}" }
                    _uiState.update {
                        ServiceFormUiState(
                            serviceId = serviceId,
                            isLoading = false,
                            error = appError,
                        )
                    }
                },
            )
        }
    }

    /**
     * Updates the service name.
     */
    fun onNameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                name = value,
                nameError = validateName(value),
            )
        }
    }

    /**
     * Updates the service description.
     */
    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    /**
     * Updates the base price.
     */
    fun onPriceChange(value: String) {
        _uiState.update { state ->
            state.copy(
                basePrice = value,
                priceError = validatePrice(value),
            )
        }
    }

    /**
     * Updates the duration.
     */
    fun onDurationChange(value: String) {
        _uiState.update { state ->
            state.copy(
                durationMinutes = value,
                durationError = validateDuration(value),
            )
        }
    }

    /**
     * Updates the category.
     */
    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(categoryId = value) }
    }

    /**
     * Updates the active status.
     */
    fun onActiveChange(value: Boolean) {
        _uiState.update { it.copy(isActive = value) }
    }

    /**
     * Saves the service (create or update).
     */
    fun saveService() {
        val state = _uiState.value

        // Validate all fields
        val nameErr = validateName(state.name)
        val priceErr = validatePrice(state.basePrice)
        val durationErr = validateDuration(state.durationMinutes)

        if (nameErr != null || priceErr != null || durationErr != null) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    priceError = priceErr,
                    durationError = durationErr,
                )
            }
            return
        }

        val price = state.parsedPrice ?: return
        val duration = state.parsedDuration ?: return

        screenModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val result = if (state.isEditMode) {
                updateServiceUseCase(
                    id = state.serviceId!!,
                    name = state.name,
                    description = state.description.takeIf { it.isNotBlank() },
                    basePrice = price,
                    durationMinutes = duration,
                    categoryId = state.categoryId.takeIf { it.isNotBlank() },
                    isActive = state.isActive,
                )
            } else {
                createServiceUseCase(
                    name = state.name,
                    description = state.description.takeIf { it.isNotBlank() },
                    basePrice = price,
                    durationMinutes = duration,
                    categoryId = state.categoryId,
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                },
                onFailure = { error ->
                    val appError = error.toAppError()
                    logger.w(appError) { "Failed to save service: ${appError::class.simpleName}" }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = appError,
                        )
                    }
                },
            )
        }
    }

    /**
     * Clears the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateName(value: String): String? {
        return when {
            value.isBlank() -> "Name is required"
            value.length < 3 -> "Name must be at least 3 characters"
            value.length > 100 -> "Name must be at most 100 characters"
            else -> null
        }
    }

    private fun validatePrice(value: String): String? {
        return when {
            value.isBlank() -> "Price is required"
            value.toDoubleOrNull() == null -> "Invalid price format"
            value.toDouble() < 0 -> "Price must be non-negative"
            else -> null
        }
    }

    private fun validateDuration(value: String): String? {
        return when {
            value.isBlank() -> "Duration is required"
            value.toIntOrNull() == null -> "Invalid duration format"
            value.toInt() < 5 -> "Duration must be at least 5 minutes"
            value.toInt() > 480 -> "Duration must be at most 480 minutes"
            else -> null
        }
    }
}
