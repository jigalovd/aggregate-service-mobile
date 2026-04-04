package com.aggregateservice.feature.catalog.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters

/**
 * UI State для экрана поиска (Presentation слой).
 *
 * @property searchQuery Текущий поисковый запрос
 * @property providers Результаты поиска
 * @property categories Доступные категории
 * @property selectedCategories Выбранные категории
 * @property filters Активные фильтры
 * @property isLoading Состояние загрузки
 * @property isLoadingMore Подгрузка следующей страницы
 * @property hasMore Есть ли ещё результаты
 * @property currentPage Текущая страница
 * @property error Ошибка (null если нет)
 * @property isFilterSheetOpen Открыт ли bottom sheet с фильтрами
 * @property recentSearches Недавние поисковые запросы
 */
@Stable
data class SearchUiState(
    val searchQuery: String = "",
    val providers: List<Provider> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val filters: SearchFilters = SearchFilters(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val error: AppError? = null,
    val isFilterSheetOpen: Boolean = false,
    val recentSearches: List<String> = emptyList(),
) {
    /**
     * Проверяет, есть ли результаты.
     */
    val hasResults: Boolean
        get() = providers.isNotEmpty()

    /**
     * Проверяет, есть ли активные фильтры.
     */
    val hasActiveFilters: Boolean
        get() =
            selectedCategories.isNotEmpty() ||
                filters.minRating != null

    /**
     * Количество активных фильтров.
     */
    val activeFiltersCount: Int
        get() {
            var count = 0
            if (selectedCategories.isNotEmpty()) count++
            if (filters.minRating != null) count++
            return count
        }

    /**
     * Проверяет, можно ли подгрузить ещё.
     */
    fun canLoadMore(): Boolean = hasMore && !isLoadingMore && !isLoading

    companion object {
        val Initial = SearchUiState()
    }
}
