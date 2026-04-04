package com.aggregateservice.feature.catalog.presentation.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aggregateservice.core.network.toAppError
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.usecase.GetCategoriesUseCase
import com.aggregateservice.feature.catalog.domain.usecase.SearchProvidersUseCase
import com.aggregateservice.feature.catalog.presentation.model.SearchUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ScreenModel для экрана поиска (Presentation слой).
 *
 * **Features:**
 * - Debounced search (300ms delay)
 * - Category filtering
 * - Rating filter
 * - Sort options
 * - Pagination
 * - Recent searches (local)
 *
 * @property searchProvidersUseCase UseCase для поиска мастеров
 * @property getCategoriesUseCase UseCase для получения категорий
 */
@OptIn(FlowPreview::class)
class SearchScreenModel(
    private val searchProvidersUseCase: SearchProvidersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ScreenModel {
    private val _uiState = MutableStateFlow(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Debounced search query
    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadCategories()

        // Debounced search - triggers 300ms after user stops typing
        searchQueryFlow
            .debounce(SEARCH_DEBOUNCE_MS)
            .onEach { query ->
                if (query.isNotBlank()) {
                    search(query)
                }
            }.launchIn(screenModelScope)
    }

    /**
     * Загружает категории для фильтров.
     */
    private fun loadCategories() {
        screenModelScope.launch {
            getCategoriesUseCase(parentId = null)
                .fold(
                    onSuccess = { categories ->
                        _uiState.value = _uiState.value.copy(categories = categories)
                    },
                    onFailure = { /* Non-critical, just log */ },
                )
        }
    }

    /**
     * Обновляет поисковый запрос.
     *
     * **Intent:** Пользователь вводит текст в поле поиска
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQueryFlow.value = query

        // Clear results if query is empty
        if (query.isBlank()) {
            _uiState.value =
                _uiState.value.copy(
                    providers = emptyList(),
                    currentPage = 0,
                    hasMore = true,
                )
        }
    }

    /**
     * Выполняет поиск.
     *
     * **Intent:** Пользователь нажал Enter или кнопку поиска
     */
    fun onSearchSubmit() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isNotBlank()) {
            search(query)
            addToRecentSearches(query)
        }
    }

    /**
     * Выполняет поиск с текущими фильтрами.
     */
    private fun search(query: String, page: Int = 1) {
        val state = _uiState.value

        _uiState.value =
            if (page == 1) {
                state.copy(isLoading = true, error = null)
            } else {
                state.copy(isLoadingMore = true)
            }

        screenModelScope.launch {
            val filters =
                state.filters.copy(
                    query = query,
                    categoryIds = state.selectedCategories.toList(),
                    page = page,
                    pageSize = PAGE_SIZE,
                )

            searchProvidersUseCase(filters)
                .fold(
                    onSuccess = { result ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                providers =
                                    if (page == 1) {
                                        result.items
                                    } else {
                                        _uiState.value.providers + result.items
                                    },
                                currentPage = page,
                                hasMore = page < result.totalPages,
                            )
                    },
                    onFailure = { error ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = error.toAppError(),
                            )
                    },
                )
        }
    }

    /**
     * Подгружает следующую страницу.
     *
     * **Intent:** Пользователь проскроллил до конца
     */
    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore()) return

        val query = state.searchQuery.trim()
        if (query.isNotBlank()) {
            search(query, state.currentPage + 1)
        }
    }

    /**
     * Выбирает/снимает категорию.
     *
     * **Intent:** Пользователь нажал на чип категории
     */
    fun onCategoryToggle(categoryId: String) {
        val state = _uiState.value
        val newCategories =
            if (categoryId in state.selectedCategories) {
                state.selectedCategories - categoryId
            } else {
                state.selectedCategories + categoryId
            }

        _uiState.value = state.copy(selectedCategories = newCategories)

        // Re-search with new filters
        if (state.searchQuery.isNotBlank()) {
            search(state.searchQuery)
        }
    }

    /**
     * Устанавливает минимальный рейтинг.
     *
     * **Intent:** Пользователь выбрал рейтинг в фильтре
     */
    fun onMinRatingChanged(rating: Double?) {
        _uiState.value =
            _uiState.value.copy(
                filters = _uiState.value.filters.copy(minRating = rating),
            )

        if (_uiState.value.searchQuery.isNotBlank()) {
            search(_uiState.value.searchQuery)
        }
    }

    /**
     * Устанавливает сортировку.
     *
     * **Intent:** Пользователь выбрал поле сортировки
     */
    fun onSortByChanged(sortBy: SearchFilters.SortBy) {
        _uiState.value =
            _uiState.value.copy(
                filters = _uiState.value.filters.copy(sortBy = sortBy),
            )

        if (_uiState.value.searchQuery.isNotBlank()) {
            search(_uiState.value.searchQuery)
        }
    }

    /**
     * Очищает все фильтры.
     *
     * **Intent:** Пользователь нажал "Сбросить фильтры"
     */
    fun onClearFilters() {
        _uiState.value =
            _uiState.value.copy(
                selectedCategories = emptySet(),
                filters = SearchFilters(),
            )

        if (_uiState.value.searchQuery.isNotBlank()) {
            search(_uiState.value.searchQuery)
        }
    }

    /**
     * Открывает/закрывает панель фильтров.
     *
     * **Intent:** Пользователь нажал кнопку фильтров
     */
    fun onFilterSheetToggle() {
        _uiState.value =
            _uiState.value.copy(
                isFilterSheetOpen = !_uiState.value.isFilterSheetOpen,
            )
    }

    /**
     * Выбирает недавний поиск.
     *
     * **Intent:** Пользователь нажал на недавний запрос
     */
    fun onRecentSearchClick(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQueryFlow.value = query
        search(query)
    }

    /**
     * Очищает ошибку.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Добавляет запрос в недавние поиски.
     */
    private fun addToRecentSearches(query: String) {
        val recent =
            _uiState.value.recentSearches
                .filterNot { it.equals(query, ignoreCase = true) }
                .take(MAX_RECENT_SEARCHES - 1)

        _uiState.value =
            _uiState.value.copy(
                recentSearches = listOf(query) + recent,
            )
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val PAGE_SIZE = 20
        private const val MAX_RECENT_SEARCHES = 5
    }
}
