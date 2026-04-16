package com.aggregateservice.feature.catalog.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import com.aggregateservice.core.favorites_api.FavoritesToggle
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import com.aggregateservice.feature.catalog.presentation.model.ProviderDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана деталей мастера (Presentation слой).
 *
 * Использует composite endpoint /providers/{id}/detail для загрузки
 * provider + services + is_favorite в одном HTTP запросе вместо трёх.
 *
 * Фильтрация по категориям — клиентская (in-memory).
 *
 * @property catalogRepository Repository с composite getProviderDetail()
 * @property favoritesToggle Для добавления/удаления из избранного
 */
class ProviderDetailScreenModel(
    private val catalogRepository: CatalogRepository,
    private val favoritesToggle: FavoritesToggle,
    private val logger: Logger,
) : ScreenModel {
    private val _uiState = MutableStateFlow<ProviderDetailUiState>(ProviderDetailUiState.Loading)
    val uiState: StateFlow<ProviderDetailUiState> = _uiState.asStateFlow()

    private var providerId: String? = null
    private var loadingId: String? = null

    fun initialize(id: String) {
        if (providerId == id) return
        if (loadingId == id) return

        loadingId = id
        providerId = id
        loadProviderDetail(id)
    }

    private fun loadProviderDetail(id: String) {
        _uiState.value = ProviderDetailUiState.Loading

        screenModelScope.launch {
            catalogRepository
                .getProviderDetail(id)
                .fold(
                    onSuccess = { detail ->
                        if (loadingId != id) return@launch
                        _uiState.value =
                            _uiState.value.copy(
                                provider = detail.provider,
                                services = detail.services,
                                isFavorite = detail.isFavorite ?: false,
                                isLoading = false,
                                isLoadingServices = false,
                                error = null,
                            )
                    },
                    onFailure = { error ->
                        if (loadingId != id) return@launch
                        val appError = error.toAppError()
                        logger.w(appError) { "Failed to load provider detail: ${appError::class.simpleName}" }
                        _uiState.value = ProviderDetailUiState.error(appError)
                    },
                )
        }
    }

    /**
     * Обновляет выбранную категорию (клиентская фильтрация).
     * Услуги загружены в памяти, дополнительный API вызов не нужен.
     */
    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

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
                    catalogRepository.invalidateCache()
                },
                onFailure = { error ->
                    val appError = error.toAppError()
                    logger.w(appError) { "Favorite toggle failed: ${appError::class.simpleName}" }
                    _uiState.value = currentState.copy(error = appError)
                },
            )
        }
    }

    fun onRefresh() {
        providerId?.let { id ->
            loadingId = id
            loadProviderDetail(id)
        }
    }

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

    fun retry() {
        providerId?.let { id ->
            loadingId = id
            loadProviderDetail(id)
        }
    }
}
