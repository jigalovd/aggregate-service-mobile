package com.aggregateservice.feature.catalog.data.api

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.catalog.data.dto.CategoryDto
import com.aggregateservice.feature.catalog.data.dto.ProviderDto
import com.aggregateservice.feature.catalog.data.dto.ServiceDto
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API сервис для Catalog Feature.
 *
 * **Responsibilities:**
 * - Выполнение HTTP запросов к Backend API
 * - Сериализация/десериализация DTO
 * - Возврат Result с DTO или ошибкой
 *
 * **Important:** Использует Ktor и никаких Android зависимостей.
 *
 * @property client HTTP клиент (Ktor)
 */
class CatalogApiService(
    private val client: HttpClient
) {
    /**
     * Поиск мастеров по фильтрам.
     *
     * **Endpoint:** GET /providers
     * **Query params:**
     * - categoryIds: List<String>
     * - lat, lon, radius: для геопоиска
     * - minRating, maxPrice: фильтры
     * - page, pageSize: пагинация
     * - sortBy, sortOrder: сортировка
     */
    suspend fun searchProviders(filters: SearchFilters): Result<List<ProviderDto>> {
        return safeApiCall<List<ProviderDto>> {
            client.get("/providers") {
                contentType(ContentType.Application.Json)
                // Category filter
                filters.categoryIds.forEach { parameter("categoryIds", it) }
                // Geo search
                filters.latitude?.let { parameter("lat", it) }
                filters.longitude?.let { parameter("lon", it) }
                filters.radiusKm?.let { parameter("radius", it) }
                // Rating filter
                filters.minRating?.let { parameter("minRating", it) }
                // Pagination
                parameter("page", filters.page)
                parameter("pageSize", filters.pageSize)
                // Sorting
                parameter("sortBy", filters.sortBy.name)
                parameter("sortOrder", filters.sortOrder.name)
            }
        }
    }

    /**
     * Получение мастера по ID.
     *
     * **Endpoint:** GET /providers/{id}
     */
    suspend fun getProviderById(providerId: String): Result<ProviderDto> {
        return safeApiCall<ProviderDto> {
            client.get("/providers/$providerId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получение списка категорий.
     *
     * **Endpoint:** GET /categories
     */
    suspend fun getCategories(parentId: String?): Result<List<CategoryDto>> {
        return safeApiCall<List<CategoryDto>> {
            client.get("/categories") {
                contentType(ContentType.Application.Json)
                parentId?.let { parameter("parentId", it) }
            }
        }
    }

    /**
     * Получение категории по ID.
     *
     * **Endpoint:** GET /categories/{id}
     */
    suspend fun getCategoryById(categoryId: String): Result<CategoryDto> {
        return safeApiCall<CategoryDto> {
            client.get("/categories/$categoryId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получение услуг мастера.
     *
     * **Endpoint:** GET /providers/{providerId}/services
     */
    suspend fun getProviderServices(
        providerId: String,
        categoryId: String?
    ): Result<List<ServiceDto>> {
        return safeApiCall<List<ServiceDto>> {
            client.get("/providers/$providerId/services") {
                contentType(ContentType.Application.Json)
                categoryId?.let { parameter("categoryId", it) }
            }
        }
    }

    /**
     * Получение услуги по ID.
     *
     * **Endpoint:** GET /services/{id}
     */
    suspend fun getServiceById(serviceId: String): Result<ServiceDto> {
        return safeApiCall<ServiceDto> {
            client.get("/services/$serviceId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Поиск услуг.
     *
     * **Endpoint:** GET /services/search
     */
    suspend fun searchServices(
        query: String,
        filters: SearchFilters
    ): Result<List<ServiceDto>> {
        return safeApiCall<List<ServiceDto>> {
            client.get("/services/search") {
                contentType(ContentType.Application.Json)
                parameter("q", query)
                filters.categoryIds.forEach { parameter("categoryIds", it) }
                parameter("page", filters.page)
                parameter("pageSize", filters.pageSize)
            }
        }
    }
}
