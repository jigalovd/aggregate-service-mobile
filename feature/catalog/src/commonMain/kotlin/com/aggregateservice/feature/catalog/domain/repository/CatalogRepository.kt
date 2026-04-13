package com.aggregateservice.feature.catalog.domain.repository

import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.ProviderDetailData
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service

/**
 * Интерфейс репозитория каталога (Domain слой).
 *
 * Отвечает за:
 * - Поиск мастеров по фильтрам
 * - Получение деталей мастера
 * - Получение списка категорий
 * - Получение услуг мастера
 *
 * **Important:** Это интерфейс из Domain слоя.
 * Реализация находится в data/repository пакете.
 */
interface CatalogRepository {
    /**
     * Выполняет поиск мастеров по фильтрам.
     *
     * @param filters Фильтры поиска
     * @return Result с SearchResult<Provider> при успехе, или AppError при ошибке
     */
    suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>>

    /**
     * Получает детали мастера по ID.
     *
     * @param providerId ID мастера
     * @return Result с Provider при успехе, или AppError при ошибке
     */
    suspend fun getProviderById(providerId: String): Result<Provider>

    /**
     * Composite: provider + services + favorite status in one request.
     *
     * @param providerId ID мастера
     * @return Result with ProviderDetailData (provider, services, isFavorite)
     */
    suspend fun getProviderDetail(providerId: String): Result<ProviderDetailData>

    /**
     * Получает список всех категорий услуг.
     *
     * @param parentId ID родительской категории (null для корневых)
     * @return Result с List<Category> при успехе, или AppError при ошибке
     */
    suspend fun getCategories(parentId: String? = null): Result<List<Category>>

    /**
     * Получает категорию по ID.
     *
     * @param categoryId ID категории
     * @return Result с Category при успехе, или AppError при ошибке
     */
    suspend fun getCategoryById(categoryId: String): Result<Category>

    /**
     * Получает список услуг мастера.
     *
     * @param providerId ID мастера
     * @param categoryId ID категории для фильтрации (опционально)
     * @return Result с List<Service> при успехе, или AppError при ошибке
     */
    suspend fun getProviderServices(
        providerId: String,
        categoryId: String? = null,
    ): Result<List<Service>>

    /**
     * Получает услугу по ID.
     *
     * @param serviceId ID услуги
     * @return Result с Service при успехе, или AppError при ошибке
     */
    suspend fun getServiceById(serviceId: String): Result<Service>

    /**
     * Выполняет поиск услуг по названию.
     *
     * @param query Поисковый запрос
     * @param filters Фильтры поиска
     * @return Result с SearchResult<Service> при успехе, или AppError при ошибке
     */
    suspend fun searchServices(
        query: String,
        filters: SearchFilters = SearchFilters.Empty,
    ): Result<SearchResult<Service>>

    /**
     * Invalidates cached data (providers, services, search results).
     * Call after mutations that change provider data (favorite toggle, review submission).
     */
    fun invalidateCache()
}
