package com.aggregateservice.feature.catalog.data.repository

import com.aggregateservice.feature.catalog.data.api.CatalogApiService
import com.aggregateservice.feature.catalog.data.mapper.CategoryMapper
import com.aggregateservice.feature.catalog.data.mapper.ProviderMapper
import com.aggregateservice.feature.catalog.data.mapper.ServiceMapper
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.domain.model.SearchResult
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository

/**
 * Реализация CatalogRepository.
 *
 * **Important:** Этот класс содержит платформенные зависимости (Ktor).
 * Используется только в data слое.
 *
 * @property apiService API сервис для каталога
 */
class CatalogRepositoryImpl(
    private val apiService: CatalogApiService,
) : CatalogRepository {
    override suspend fun searchProviders(filters: SearchFilters): Result<SearchResult<Provider>> {
        val result = apiService.searchProviders(filters)

        return result.fold(
            onSuccess = { response ->
                val domainProviders = response.providers.map { ProviderMapper.toDomain(it) }
                Result.success(
                    SearchResult(
                        items = domainProviders,
                        totalCount = response.total,
                        totalPages = (response.total + response.limit - 1) / response.limit,
                        currentPage = filters.page,
                    ),
                )
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getProviderById(providerId: String): Result<Provider> {
        val result = apiService.getProviderById(providerId)

        return result.fold(
            onSuccess = { response ->
                Result.success(ProviderMapper.toDomain(response.data))
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getCategories(parentId: String?): Result<List<Category>> {
        val result = apiService.getCategories(parentId)

        return result.fold(
            onSuccess = { response ->
                val domainCategories = response.categories.map { CategoryMapper.toDomain(it) }
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
            onSuccess = { category ->
                Result.success(CategoryMapper.toDomain(category))
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }

    override suspend fun getProviderServices(
        providerId: String,
        categoryId: String?,
    ): Result<List<Service>> {
        val result = apiService.getProviderServices(providerId, categoryId)

        return result.fold(
            onSuccess = { response ->
                val domainServices = response.services.map { ServiceMapper.toDomain(it) }
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
            onSuccess = { service ->
                Result.success(ServiceMapper.toDomain(service))
            },
            onFailure = { error ->
                Result.failure(error)
            },
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
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }
}
