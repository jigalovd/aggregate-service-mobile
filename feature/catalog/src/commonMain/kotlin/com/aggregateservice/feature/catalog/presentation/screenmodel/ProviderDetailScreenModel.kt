package com.aggregateservice.feature.catalog.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.core.favorites_api.FavoritesToggle
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
    private val favoritesToggle: FavoritesToggle,
    private val logger: Logger,
) : ScreenModel {
    // UI State
    private val _uiState = MutableStateFlow<ProviderDetailUiState>(ProviderDetailUiState.Loading)
    val uiState: StateFlow<ProviderDetailUiState> = _uiState.asStateFlow()

    // Provider ID для загрузки
    private var providerId: String? = null

    // Loading ID guard для предотвращения race conditions
    private var loadingId: String? = null

    /**
     * Инициализирует ScreenModel с ID мастера.
     *
     * **Intent:** Экран открыт с providerId
     *
     * @param id ID мастера
     */
    fun initialize(id: String) {
        if (providerId == id) return // Already loaded
        if (loadingId == id) return // Loading already in progress for this id

        loadingId = id
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
                        // Check that we're still loading the same provider (race condition guard)
                        if (loadingId != id) return@launch

                        val isFavorite = favoritesToggle.isFavorite(id).getOrElse { false }
                        _uiState.value =
                            _uiState.value.copy(
                                provider = provider,
                                isLoading = false,
                                error = null,
                                isFavorite = isFavorite,
                            )
                    },
                    onFailure = { error ->
                        if (loadingId != id) return@launch
                        val appError = error.toAppError()
                        logger.w(appError) { "Failed to load provider details: ${appError::class.simpleName}" }
                        _uiState.value = ProviderDetailUiState.error(appError)
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
                        _uiState.value =
                            _uiState.value.copy(
                                services = services,
                                isLoadingServices = false,
                            )
                    },
                    onFailure = { error ->
                        val appError = (error as? AppError) ?: error.toAppError()
                        logger.w(appError) { "Failed to load provider services, continuing" }
                        _uiState.value =
                            _uiState.value.copy(
                                isLoadingServices = false,
                            )
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
        val providerId = currentState.provider?.id ?: return

        screenModelScope.launch {
            val result =
                if (currentState.isFavorite) {
                    favoritesToggle.removeFavorite(providerId)
                } else {
                    favoritesToggle.addFavorite(providerId)
                }

            result.fold(
                onSuccess = {
                    _uiState.value = currentState.copy(isFavorite = !currentState.isFavorite)
                },
                onFailure = { error ->
                    val appError = error.toAppError()
                    logger.w(appError) { "Favorite toggle failed: ${appError::class.simpleName}" }
                    _uiState.value =
                        currentState.copy(
                            error = appError,
                        )
                },
            )
        }
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

    fun onServiceToggle(serviceId: String) {
        val current = _uiState.value.selectedServiceIds
        val newSelection =
            if (serviceId in current) {
                current - serviceId
            } else {
                current + serviceId
            }
        _uiState.value = _uiState.value.copy(selectedServiceIds = newSelection)
    }

    /**
     * Повторяет загрузку после ошибки.
     *
     * **Intent:** Пользователь нажал "Повторить" на экране ошибки
     */
    fun retry() {
        providerId?.let { id ->
            loadingId = null // Reset so retry works
            loadProviderDetails(id)
            loadProviderServices(id)
        }
    }
}
