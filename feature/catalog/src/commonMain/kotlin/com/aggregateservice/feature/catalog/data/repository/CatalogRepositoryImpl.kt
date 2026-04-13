package com.aggregateservice.feature.catalog.data.repository

import com.aggregateservice.feature.catalog.data.api.CatalogApiService
import com.aggregateservice.feature.catalog.data.mapper.CategoryMapper
import com.aggregateservice.feature.catalog.data.mapper.ProviderMapper
import com.aggregateservice.feature.catalog.data.mapper.ServiceMapper
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.ProviderDetailData
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Реализация CatalogRepository с TTL-кэшированием.
 *
 * **Important:** Этот класс содержит платформенные зависимости (Ktor).
 * Используется только в data слое.
 *
 * Кэш хранится в памяти и переживает пересоздание ScreenModel (Koin singleton).
 * Thread safety: MutableMap доступен из Dispatchers.Main.
 * В худшем случае при гонке = лишний API-вызов, не повреждение данных.
 *
 * @property apiService API сервис для каталога
 * @property timeSource Источник времени для TTL (инжектируемый для тестов)
 */
class CatalogRepositoryImpl(
    private val apiService: CatalogApiService,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : CatalogRepository {

    private data class CacheEntry<T>(
        val value: T,
        val timestamp: TimeMark,
    )

    // Private cache key generator — keeps domain model clean
    // GPS coordinates rounded to 3 decimals (~111m) to avoid cache misses on GPS drift
    private fun SearchFilters.toCacheKey(): String {
        val latKey = latitude?.let { "%.3f".format(it) } ?: "N"
        val lonKey = longitude?.let { "%.3f".format(it) } ?: "N"
        return "${page}_${pageSize}_" +
            "${categoryIds.sorted().joinToString(",")}_" +
            "${latKey}_${lonKey}_${radiusKm ?: "N"}_" +
            "${sortBy}_${sortOrder}_" +
            "${minRating ?: "N"}_${isVerified ?: "N"}_" +
            "${query ?: "N"}"
    }

    private fun <T> MutableMap<String, CacheEntry<T>>.getIfFresh(key: String, ttlMs: Long): T? {
        val entry = this[key] ?: return null
        val elapsed = entry.timestamp.elapsedNow().inWholeMilliseconds
        return if (elapsed < ttlMs) entry.value else null.also { remove(key) }
    }

    private fun <T> MutableMap<String, CacheEntry<T>>.put(key: String, value: T) {
        this[key] = CacheEntry(value, timeSource.markNow())
    }

    // Thread safety: MutableMap accessed from Dispatchers.Main.
    // Worst case under race = extra API call, not corruption. Acceptable for MVP.
    private val categoriesCache = mutableMapOf<String, CacheEntry<List<Category>>>()
    private val searchCache = mutableMapOf<String, CacheEntry<SearchResult<Provider>>>()
    private val providerCache = mutableMapOf<String, CacheEntry<Provider>>()
    private val servicesCache = mutableMapOf<String, CacheEntry<List<Service>>>()

    /**
     * Explicit cache invalidation for mutation-triggered staleness.
     * Call after favorites toggle, review submission, etc.
     */
    override fun invalidateCache() {
        searchCache.clear()
        providerCache.clear()
        servicesCache.clear()
        // categoriesCache not cleared — categories rarely change
    }

    override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
        val cacheKey = "search_${filters.toCacheKey()}"
        searchCache.getIfFresh<SearchResult<Provider>>(cacheKey, SEARCH_TTL_MS)?.let {
            return Result.success(it)
        }

        val result = apiService.searchProviders(filters)

        return result.fold(
            onSuccess = { response ->
                val domainProviders = response.providers.map { ProviderMapper.toDomain(it) }
                val searchResult = SearchResult(
                    items = domainProviders,
                    totalCount = response.total,
                    totalPages = (response.total + response.limit - 1) / response.limit,
                    currentPage = filters.page,
                )
                searchCache.put(cacheKey, searchResult)
                Result.success(searchResult)
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getProviderById(providerId: String): Result<Provider> {
        providerCache.getIfFresh<Provider>(providerId, PROVIDER_TTL_MS)?.let {
            return Result.success(it)
        }

        val result = apiService.getProviderById(providerId)

        return result.fold(
            onSuccess = { providerDto ->
                val provider = ProviderMapper.toDomain(providerDto)
                providerCache.put(providerId, provider)
                Result.success(provider)
            },
            onFailure = { error: Throwable ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getProviderDetail(providerId: String): Result<ProviderDetailData> {
        val result = apiService.getProviderDetail(providerId)

        return result.fold(
            onSuccess = { compositeDto ->
                val provider = ProviderMapper.toDomain(compositeDto.provider)
                val services = compositeDto.services.map { ServiceMapper.toDomain(it) }
                providerCache.put(providerId, provider)
                Result.success(
                    ProviderDetailData(
                        provider = provider,
                        services = services,
                        isFavorite = compositeDto.isFavorite,
                    ),
                )
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getCategories(parentId: String?): Result<List<Category>> {
        val cacheKey = "cat_${parentId ?: "root"}"
        categoriesCache.getIfFresh<List<Category>>(cacheKey, CATEGORIES_TTL_MS)?.let {
            return Result.success(it)
        }

        val result = apiService.getCategories(parentId)

        return result.fold(
            onSuccess = { response ->
                val domainCategories = response.categories.map { CategoryMapper.toDomain(it) }
                categoriesCache.put(cacheKey, domainCategories)
                Result.success(domainCategories)
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getCategoryById(categoryId: String): Result<Category> {
        val result = apiService.getCategoryById(categoryId)
        return result.fold(
            onSuccess = { Result.success(CategoryMapper.toDomain(it)) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getProviderServices(
        providerId: String,
        categoryId: String?,
    ): Result<List<Service>> {
        val cacheKey = "svc_${providerId}_${categoryId ?: "all"}"
        servicesCache.getIfFresh<List<Service>>(cacheKey, SERVICES_TTL_MS)?.let {
            return Result.success(it)
        }

        val result = apiService.getProviderServices(providerId, categoryId)

        return result.fold(
            onSuccess = { services ->
                val domainServices = services.map { ServiceMapper.toDomain(it) }
                servicesCache.put(cacheKey, domainServices)
                Result.success(domainServices)
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getServiceById(serviceId: String): Result<Service> {
        val result = apiService.getServiceById(serviceId)
        return result.fold(
            onSuccess = { Result.success(ServiceMapper.toDomain(it)) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun searchServices(
        query: String,
        filters: SearchFilters,
    ): Result<SearchResult<Service>> {
        val result = apiService.searchServices(query, filters)
        return result.fold(
            onSuccess = { services ->
                val domainServices = services.map { ServiceMapper.toDomain(it) }
                Result.success(
                    SearchResult(
                        items = domainServices,
                        totalCount = domainServices.size,
                        totalPages = 1,
                        currentPage = filters.page,
                    ),
                )
            },
            onFailure = { Result.failure(it) },
        )
    }

    companion object {
        private const val CATEGORIES_TTL_MS = 30 * 60 * 1000L  // 30 minutes
        private const val SEARCH_TTL_MS = 2 * 60 * 1000L       // 2 minutes
        private const val PROVIDER_TTL_MS = 5 * 60 * 1000L     // 5 minutes
        private const val SERVICES_TTL_MS = 5 * 60 * 1000L     // 5 minutes
    }
}
