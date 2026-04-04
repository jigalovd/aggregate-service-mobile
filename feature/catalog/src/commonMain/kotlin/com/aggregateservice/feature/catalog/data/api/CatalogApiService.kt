package com.aggregateservice.feature.catalog.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.catalog.data.dto.CategoriesResponseDto
import com.aggregateservice.feature.catalog.data.dto.CategoryDto
import com.aggregateservice.feature.catalog.data.dto.ServiceDto
import com.aggregateservice.feature.catalog.data.dto.request.ProviderSearchRequestDto
import com.aggregateservice.feature.catalog.data.dto.response.ProviderDetailsResponseDto
import com.aggregateservice.feature.catalog.data.dto.response.ProviderSearchResponseDto
import com.aggregateservice.feature.catalog.data.dto.response.ServiceListResponseDto
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import io.ktor.client.HttpClient
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
    private val client: HttpClient,
) {
    /**
     * Поиск мастеров по фильтрам.
     *
     * **Endpoint:** POST /api/v1/catalog/providers/search
     *
     * Если геолокация недоступна, используем дефолтные координаты Haifa (32.8, 35.0)
     * согласно iOS stub паттерну — это позволяет получить релевантных провайдеров
     */
    suspend fun searchProviders(filters: SearchFilters): Result<ProviderSearchResponseDto> {
        return safeApiCall<ProviderSearchResponseDto> {
            // Default to Haifa coordinates when geo not available
            val lat = filters.latitude ?: HAIFA_LAT
            val lon = filters.longitude ?: HAIFA_LON

            val requestDto = ProviderSearchRequestDto(
                lat = lat,
                lon = lon,
                radiusKm = filters.radiusKm ?: DEFAULT_RADIUS_KM,
                categoryId = filters.categoryIds.firstOrNull(),
                sortBy = when (filters.sortBy) {
                    com.aggregateservice.feature.catalog.domain.model.SearchFilters.SortBy.DISTANCE -> "distance"
                    com.aggregateservice.feature.catalog.domain.model.SearchFilters.SortBy.RATING -> "rating"
                    else -> "rating"
                },
                limit = filters.pageSize,
                offset = filters.offset,
            )
            client.post("/api/v1/catalog/providers/search") {
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }
        }
    }

    private companion object {
        const val HAIFA_LAT = 32.8
        const val HAIFA_LON = 35.0
        const val DEFAULT_RADIUS_KM = 30.0
    }

    /**
     * Получение мастера по ID.
     *
     * **Endpoint:** GET /api/v1/catalog/providers/{id}
     */
    suspend fun getProviderById(providerId: String): Result<ProviderDetailsResponseDto> {
        return safeApiCall<ProviderDetailsResponseDto> {
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
    suspend fun getCategories(parentId: String?): Result<CategoriesResponseDto> {
        return safeApiCall<CategoriesResponseDto> {
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
        categoryId: String?,
    ): Result<ServiceListResponseDto> {
        return safeApiCall<ServiceListResponseDto> {
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
        filters: SearchFilters,
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
