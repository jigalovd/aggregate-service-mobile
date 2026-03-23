package com.aggregateservice.feature.catalog.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.catalog.domain.usecase.GetProviderDetailsUseCase
import com.aggregateservice.feature.catalog.domain.usecase.GetProviderServicesUseCase
import com.aggregateservice.feature.catalog.presentation.model.ProviderDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана деталей мастера (Presentation слой).
 *
 * **Architecture:**
 * - ScreenModel (Voyager) = ViewModel в MVVM архитектуре
 * - Хранит UI state
 * - Обрабатывает пользовательские действия (intents)
 * - Вызывает UseCases из Domain слоя
 *
 * **UDF Pattern:** Unidirectional Data Flow
 * - UI отображает state
 * - UI отправляет intents (события)
 * - ScreenModel обрабатывает intents и обновляет state
 *
 * @property getProviderDetailsUseCase UseCase для получения деталей мастера
 * @property getProviderServicesUseCase UseCase для получения услуг мастера
 */
class ProviderDetailScreenModel(
    private val getProviderDetailsUseCase: GetProviderDetailsUseCase,
    private val getProviderServicesUseCase: GetProviderServicesUseCase,
) : ScreenModel {

    // UI State
    private val _uiState = MutableStateFlow<ProviderDetailUiState>(ProviderDetailUiState.Loading)
    val uiState: StateFlow<ProviderDetailUiState> = _uiState.asStateFlow()

    // Provider ID для загрузки
    private var providerId: String? = null

    /**
     * Инициализирует ScreenModel с ID мастера.
     *
     * **Intent:** Экран открыт с providerId
     *
     * @param id ID мастера
     */
    fun initialize(id: String) {
        if (providerId == id) return // Already loaded

        providerId = id
        loadProviderDetails(id)
        loadProviderServices(id)
    }

    /**
     * Загружает детали мастера.
     *
     * **Intent:** Пользователь открыл экран или потянул для обновления
     */
    private fun loadProviderDetails(id: String) {
        _uiState.value = ProviderDetailUiState.Loading

        screenModelScope.launch {
            getProviderDetailsUseCase(id)
                .fold(
                    onSuccess = { provider ->
                        _uiState.value = _uiState.value.copy(
                            provider = provider,
                            isLoading = false,
                            error = null,
                            isFavorite = false, // TODO: Check favorite status
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = ProviderDetailUiState.error(error.toAppError())
                    },
                )
        }
    }

    /**
     * Загружает услуги мастера.
     *
     * **Intent:** Пользователь открыл экран или изменил фильтр категории
     */
    private fun loadProviderServices(id: String, categoryId: String? = null) {
        _uiState.value = _uiState.value.copy(isLoadingServices = true)

        screenModelScope.launch {
            getProviderServicesUseCase(id, categoryId)
                .fold(
                    onSuccess = { services ->
                        _uiState.value = _uiState.value.copy(
                            services = services,
                            isLoadingServices = false,
                        )
                    },
                    onFailure = { error ->
                        // Ошибка загрузки услуг не критична - сохраняем state, но логируем
                        _uiState.value = _uiState.value.copy(
                            isLoadingServices = false,
                        )
                        println("Failed to load provider services: ${error.message}")
                    },
                )
        }
    }

    /**
     * Обновляет выбранную категорию для фильтрации услуг.
     *
     * **Intent:** Пользователь выбрал категорию в фильтре
     *
     * @param categoryId ID категории или null для всех услуг
     */
    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)

        // Перезагружаем услуги с фильтром
        providerId?.let { id ->
            loadProviderServices(id, categoryId)
        }
    }

    /**
     * Переключает статус избранного.
     *
     * **Intent:** Пользователь нажал кнопку "В избранное"
     */
    fun onFavoriteToggle() {
        val currentState = _uiState.value

        // TODO: Implement with AddToFavoritesUseCase / RemoveFromFavoritesUseCase
        // For now, just toggle the local state
        _uiState.value = currentState.copy(
            isFavorite = !currentState.isFavorite,
        )
    }

    /**
     * Обновляет данные (pull-to-refresh).
     *
     * **Intent:** Пользователь потянул для обновления
     */
    fun onRefresh() {
        providerId?.let { id ->
            loadProviderDetails(id)
            loadProviderServices(id, _uiState.value.selectedCategoryId)
        }
    }

    /**
     * Очищает ошибку.
     *
     * **Intent:** Пользователь закрыл error dialog
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Повторяет загрузку после ошибки.
     *
     * **Intent:** Пользователь нажал "Повторить" на экране ошибки
     */
    fun retry() {
        providerId?.let { id ->
            loadProviderDetails(id)
            loadProviderServices(id)
        }
    }
}
