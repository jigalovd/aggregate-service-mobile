package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository

/**
 * UseCase для поиска мастеров (Domain слой).
 *
 * **Responsibilities:**
 * - Валидация фильтров поиска (бизнес-правила)
 * - Вызов репозитория
 * - Преобразование ошибок
 *
 * **Important:** UseCase НЕ должен:
 * - Импортировать классы из io.ktor.*
 * - Импортировать Android/iOS классы
 * - Содержать логику UI (Compose код)
 *
 * @property repository Репозиторий каталога
 */
class SearchProvidersUseCase(
    private val repository: CatalogRepository,
) {
    /**
     * Выполняет поиск мастеров.
     *
     * @param filters Фильтры поиска
     * @return Result с SearchResult<Provider> при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(filters: SearchFilters): Result<SearchResult<Provider>> {
        // Бизнес-валидация
        filters.page.let { page ->
            if (page < 1) {
                return Result.failure(
                    AppError.ValidationError(
                        field = "page",
                        message = "Page must be >= 1",
                    ),
                )
            }
        }

        filters.pageSize.let { pageSize ->
            if (pageSize < 1 || pageSize > 100) {
                return Result.failure(
                    AppError.ValidationError(
                        field = "pageSize",
                        message = "PageSize must be between 1 and 100",
                    ),
                )
            }
        }

        // Геопоиск требует координаты и радиус
        if (filters.isGeoSearch) {
            if (filters.radiusKm != null && filters.radiusKm <= 0) {
                return Result.failure(
                    AppError.ValidationError(
                        field = "radiusKm",
                        message = "Radius must be positive",
                    ),
                )
            }
        }

        // Вызов репозитория
        return repository.searchProviders(filters)
    }
}
