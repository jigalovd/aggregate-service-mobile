package com.aggregateservice.feature.catalog.presentation.model

import androidx.compose.runtime.Stable
import com.aggregateservice.core.common.model.Location
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.presentation.screenmodel.ProviderWithDistance

/**
 * UI State для Catalog экрана (Presentation слой).
 *
 * **UDF Pattern:** Unidirectional Data Flow
 * - UI отображает state
 * - UI отправляет intents (события)
 * - ScreenModel обрабатывает intents и обновляет state
 *
 * **Compose Optimization:**
 * - @Stable аннотация позволяет Compose compiler оптимизировать рекомпозиции
 * - Все параметры immutable (val), что гарантирует стабильность
 *
 * @property isLoading Флаг загрузки данных
 * @property providers Список мастеров (от API)
 * @property categories Список категорий
 * @property searchQuery Поисковый запрос
 * @property selectedCategory Выбранная категория
 * @property filters Примененные фильтры
 * @property error Ошибка при загрузке (если есть)
 * @property hasMore Есть ли ещё результаты для подгрузки
 * @property currentPage Текущая страница пагинации
 */
@Stable
data class CatalogUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val providers: List<ProviderWithDistance> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val filters: SearchFilters = SearchFilters(),
    val error: AppError? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val userLocation: Location? = null,
) {
    companion object {
        val Initial = CatalogUiState()
    }

    /**
     * Проверяет, можно ли загрузить ещё данные.
     */
    fun canLoadMore(): Boolean = !isLoading && !isLoadingMore && hasMore

    /**
     * Проверяет, есть ли результаты поиска.
     */
    fun hasResults(): Boolean = providers.isNotEmpty()

    /**
     * Проверяет, пустой ли результат (после загрузки).
     */
    fun isEmpty(): Boolean = !isLoading && providers.isEmpty() && error == null

    /**
     * Возвращает текст для сортировки.
     */
    val sortText: String
        get() = "${filters.sortBy.displayName} (${filters.sortOrder.displayName})"

    /**
     * Проверяет, применены ли какие-либо фильтры.
     */
    fun hasActiveFilters(): Boolean =
        selectedCategory != null ||
            filters.minRating != null ||
            filters.latitude != null ||
            filters.categoryIds.isNotEmpty()

    /**
     * Количество активных фильтров.
     */
    val activeFiltersCount: Int
        get() =
            listOfNotNull(
                selectedCategory,
                filters.minRating,
                filters.latitude,
            ).count() + filters.categoryIds.size
}
