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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
     * **Endpoint:** POST /api/v1/catalog/providers/search
     */
    suspend fun searchProviders(filters: SearchFilters): Result<List<ProviderDto>> {
        return safeApiCall<List<ProviderDto>> {
            client.post("/api/v1/catalog/providers/search") {
                contentType(ContentType.Application.Json)
                setBody(filters)
            }
        }
    }

    /**
     * Получение мастера по ID.
     *
     * **Endpoint:** GET /api/v1/catalog/providers/{id}
     */
    suspend fun getProviderById(providerId: String): Result<ProviderDto> {
        return safeApiCall<ProviderDto> {
            client.get("/api/v1/catalog/providers/$providerId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получение списка категорий.
     *
     * **Endpoint:** GET /api/v1/catalog/categories
     */
    suspend fun getCategories(parentId: String?): Result<List<CategoryDto>> {
        return safeApiCall<List<CategoryDto>> {
            client.get("/api/v1/catalog/categories") {
                contentType(ContentType.Application.Json)
                parentId?.let { parameter("parentId", it) }
            }
        }
    }

    /**
     * Получение категории по ID.
     *
     * **Endpoint:** GET /api/v1/catalog/categories/{id}
     */
    suspend fun getCategoryById(categoryId: String): Result<CategoryDto> {
        return safeApiCall<CategoryDto> {
            client.get("/api/v1/catalog/categories/$categoryId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получение услуг мастера.
     *
     * **Endpoint:** GET /api/v1/catalog/providers/{providerId}/services
     */
    suspend fun getProviderServices(
        providerId: String,
        categoryId: String?
    ): Result<List<ServiceDto>> {
        return safeApiCall<List<ServiceDto>> {
            client.get("/api/v1/catalog/providers/$providerId/services") {
                contentType(ContentType.Application.Json)
                categoryId?.let { parameter("categoryId", it) }
            }
        }
    }

    /**
     * Получение услуги по ID.
     *
     * **Endpoint:** GET /api/v1/catalog/services/{id}
     */
    suspend fun getServiceById(serviceId: String): Result<ServiceDto> {
        return safeApiCall<ServiceDto> {
            client.get("/api/v1/catalog/services/$serviceId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Поиск услуг.
     *
     * **Endpoint:** GET /api/v1/catalog/services/search
     */
    suspend fun searchServices(
        query: String,
        filters: SearchFilters
    ): Result<List<ServiceDto>> {
        return safeApiCall<List<ServiceDto>> {
            client.get("/api/v1/catalog/services/search") {
                contentType(ContentType.Application.Json)
                parameter("q", query)
                filters.categoryIds.forEach { parameter("categoryIds", it) }
                parameter("page", filters.page)
                parameter("pageSize", filters.pageSize)
            }
        }
    }
}
