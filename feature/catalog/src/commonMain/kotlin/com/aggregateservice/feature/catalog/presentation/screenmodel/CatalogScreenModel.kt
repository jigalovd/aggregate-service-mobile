package com.aggregateservice.feature.catalog.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.location.LocationAccuracy
import com.aggregateservice.core.location.LocationPermissionStatus
import com.aggregateservice.core.location.LocationProvider
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchFilters.SortBy
import com.aggregateservice.feature.catalog.domain.model.SearchFilters.SortOrder
import com.aggregateservice.feature.catalog.domain.usecase.GetCategoriesUseCase
import com.aggregateservice.feature.catalog.domain.usecase.SearchProvidersUseCase
import com.aggregateservice.feature.catalog.presentation.model.CatalogUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана каталога мастеров (Presentation слой).
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
 * **Important:** ScreenModel НЕ должен:
 * - Вызывать напрямую Repository или Ktor
 * - Импортировать Android/iOS классы
 * - Содержать бизнес-логику (только UI-логику)
 *
 * @property searchProvidersUseCase UseCase для поиска мастеров
 * @property getCategoriesUseCase UseCase для получения категорий
 */
class CatalogScreenModel(
    private val searchProvidersUseCase: SearchProvidersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val locationProvider: LocationProvider,
) : ScreenModel {
    // UI State
    private val _uiState = MutableStateFlow(CatalogUiState.Initial)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        requestLocationAndSearch()
    }

    /**
     * Загружает список категорий.
     *
     * **Intent:** Пользователь открыл экран
     */
    private fun loadCategories() {
        screenModelScope.launch {
            getCategoriesUseCase(parentId = null)
                .fold(
                    onSuccess = { categories ->
                        _uiState.value =
                            _uiState.value.copy(
                                categories = categories,
                            )
                    },
                    onFailure = { _ ->
                        // Ошибка загрузки категорий не критична - UI отобразит пустой список
                    },
                )
        }
    }

    /**
     * Выполняет поиск мастеров с текущими фильтрами.
     *
     * **Intent:** Пользователь открыл экран или изменил фильтры
     */
    fun searchProviders() {
        val state = _uiState.value
        if (state.isLoading) return

        _uiState.value =
            state.copy(
                isLoading = true,
                error = null,
            )

        screenModelScope.launch {
            val filters =
                state.filters.copy(
                    categoryIds = state.selectedCategory?.let { listOf(it.id) } ?: emptyList(),
                    page = 1,
                )

            searchProvidersUseCase(filters)
                .fold(
                    onSuccess = { searchResult ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                providers = searchResult.items,
                                hasMore = searchResult.currentPage < searchResult.totalPages,
                                currentPage = 1,
                            )
                    },
                    onFailure = { error ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                error = error.toAppError(),
                            )
                    },
                )
        }
    }

    /**
     * Подгружает следующую страницу мастеров.
     *
     * **Intent:** Пользователь проскроллил до конца списка
     */
    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore()) return

        _uiState.value = state.copy(isLoadingMore = true)

        screenModelScope.launch {
            val nextPage = state.currentPage + 1
            val filters =
                state.filters.copy(
                    categoryIds = state.selectedCategory?.let { listOf(it.id) } ?: emptyList(),
                    page = nextPage,
                )

            searchProvidersUseCase(filters)
                .fold(
                    onSuccess = { searchResult ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoadingMore = false,
                                providers = state.providers + searchResult.items,
                                hasMore = searchResult.currentPage < searchResult.totalPages,
                                currentPage = nextPage,
                            )
                    },
                    onFailure = { error ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoadingMore = false,
                                error = error.toAppError(),
                            )
                    },
                )
        }
    }

    /**
     * Обновляет поисковый запрос.
     *
     * **Intent:** Пользователь ввел текст в поле поиска
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Выбирает категорию.
     *
     * **Intent:** Пользователь выбрал категорию из списка
     */
    fun onCategorySelected(category: Category?) {
        _uiState.value =
            _uiState.value.copy(
                selectedCategory = category,
            )
        searchProviders()
    }

    /**
     * Обновляет минимальный рейтинг.
     *
     * **Intent:** Пользователь изменил фильтр рейтинга
     */
    fun onMinRatingChanged(rating: Double?) {
        _uiState.value =
            _uiState.value.copy(
                filters = _uiState.value.filters.copy(minRating = rating),
            )
        searchProviders()
    }

    /**
     * Обновляет сортировку.
     *
     * **Intent:** Пользователь выбрал поле сортировки
     */
    fun onSortByChanged(sortBy: SortBy) {
        _uiState.value =
            _uiState.value.copy(
                filters = _uiState.value.filters.copy(sortBy = sortBy),
            )
        searchProviders()
    }

    /**
     * Обновляет порядок сортировки.
     *
     * **Intent:** Пользователь изменил порядок сортировки
     */
    fun onSortOrderChanged(sortOrder: SortOrder) {
        _uiState.value =
            _uiState.value.copy(
                filters = _uiState.value.filters.copy(sortOrder = sortOrder),
            )
        searchProviders()
    }

    /**
     * Сбрасывает все фильтры.
     *
     * **Intent:** Пользователь нажал "Сбросить фильтры"
     */
    fun onClearFilters() {
        _uiState.value =
            _uiState.value.copy(
                selectedCategory = null,
                searchQuery = "",
                filters = SearchFilters(),
            )
        searchProviders()
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
     * Выполняет поиск при отправке формы.
     *
     * **Intent:** Пользователь нажал Enter или кнопку поиска
     */
    fun onSearchSubmit() {
        searchProviders()
    }

    /**
     * Requests location permission and performs search with geo filters if granted.
     * Falls back to non-geo search if permission denied (D-03).
     */
    private fun requestLocationAndSearch() {
        screenModelScope.launch {
            try {
                val status = locationProvider.requestPermission()

                when (status) {
                    is LocationPermissionStatus.Granted -> {
                        val locationResult = locationProvider.getCurrentLocation(LocationAccuracy.MEDIUM)
                        locationResult.fold(
                            onSuccess = { location ->
                                _uiState.value =
                                    _uiState.value.copy(
                                        filters =
                                            _uiState.value.filters.copy(
                                                latitude = location.latitude,
                                                longitude = location.longitude,
                                                radiusKm = DEFAULT_RADIUS_KM,
                                            ),
                                    )
                            },
                            onFailure = { /* Fall back to non-geo search */ },
                        )
                        searchProviders()
                    }
                    is LocationPermissionStatus.Denied,
                    is LocationPermissionStatus.DeniedPermanently,
                    is LocationPermissionStatus.Unknown,
                    -> {
                        searchProviders() // Without geo filters
                    }
                }
            } catch (e: Exception) {
                // Fallback: if anything fails, just search without geo
                searchProviders()
            }
        }
    }

    companion object {
        private const val DEFAULT_RADIUS_KM = 30.0
    }
}
