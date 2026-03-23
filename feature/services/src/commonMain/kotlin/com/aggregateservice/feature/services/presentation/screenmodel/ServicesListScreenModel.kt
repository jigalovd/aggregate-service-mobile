package com.aggregateservice.feature.services.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.services.domain.model.ProviderService
import com.aggregateservice.feature.services.domain.usecase.DeleteServiceUseCase
import com.aggregateservice.feature.services.domain.usecase.GetServicesUseCase
import com.aggregateservice.feature.services.presentation.model.ServicesListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for services list screen.
 *
 * **Responsibilities:**
 * - Loading provider services
 * - Managing delete confirmation
 * - Handling delete action
 *
 * @property getServicesUseCase UseCase for loading services
 * @property deleteServiceUseCase UseCase for deleting service
 */
class ServicesListScreenModel(
    private val getServicesUseCase: GetServicesUseCase,
    private val deleteServiceUseCase: DeleteServiceUseCase,
) : ScreenModel {

    private val _uiState = MutableStateFlow(ServicesListUiState.Loading)
    val uiState: StateFlow<ServicesListUiState> = _uiState.asStateFlow()

    /**
     * Loads all services for the provider.
     */
    fun loadServices() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getServicesUseCase().fold(
                onSuccess = { services ->
                    _uiState.update {
                        ServicesListUiState(
                            services = services,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        ServicesListUiState.error(error.toAppError())
                    }
                },
            )
        }
    }

    /**
     * Shows delete confirmation dialog for a service.
     *
     * @param service Service to delete
     */
    fun confirmDelete(service: ProviderService) {
        _uiState.update { it.copy(serviceToDelete = service) }
    }

    /**
     * Dismisses delete confirmation dialog.
     */
    fun dismissDeleteDialog() {
        _uiState.update { it.copy(serviceToDelete = null) }
    }

    /**
     * Deletes the service pending confirmation.
     */
    fun deleteService() {
        val serviceToDelete = _uiState.value.serviceToDelete ?: return

        screenModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            deleteServiceUseCase(serviceToDelete.id).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            services = state.services.filter { it.id != serviceToDelete.id },
                            serviceToDelete = null,
                            isDeleting = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            serviceToDelete = null,
                            isDeleting = false,
                            error = error.toAppError(),
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
}
