package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository

/**
 * UseCase для получения списка категорий услуг (Domain слой).
 *
 * **Responsibilities:**
 * - Получение категорий из репозитория
 * - Поддержка вложенных категорий
 * - Кэширование (в будущем)
 *
 * @property repository Репозиторий каталога
 */
class GetCategoriesUseCase(
    private val repository: CatalogRepository,
) {
    /**
     * Получает список категорий.
     *
     * @param parentId ID родительской категории (null для корневых)
     * @return Result с List<Category> при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(parentId: String? = null): Result<List<Category>> {
        return repository.getCategories(parentId)
    }

    /**
     * Получает только корневые категории.
     *
     * @return Result с List<Category> при успехе, или AppError при ошибке
     */
    suspend fun getRootCategories(): Result<List<Category>> {
        return repository.getCategories(null)
    }
}
