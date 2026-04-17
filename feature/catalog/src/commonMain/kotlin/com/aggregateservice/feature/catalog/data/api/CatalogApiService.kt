package com.aggregateservice.feature.catalog.data.api

import com.aggregateservice.core.api.models.CategoryListResponse
import com.aggregateservice.core.api.models.CategoryResponse
import com.aggregateservice.core.api.models.ProviderDetailResponse
import com.aggregateservice.core.api.models.ProviderListResponse
import com.aggregateservice.core.api.models.ProviderResponse
import com.aggregateservice.core.api.models.ProviderSearchRequest
import com.aggregateservice.core.api.models.PublicProviderServiceItemResponse
import com.aggregateservice.core.api.models.PublicProviderServicesResponse
import com.aggregateservice.core.api.models.ServiceResponse
import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API service for Catalog Feature.
 *
 * **Responsibilities:**
 * - Execute HTTP requests to Backend API
 * - Serialize/deserialize generated DTOs from :core:api-models
 * - Return Result with generated DTO type or error
 *
 * **Important:** Uses Ktor and no Android dependencies.
 *
 * @property client HTTP client (Ktor)
 */
class CatalogApiService(
    private val client: HttpClient,
) {
    /**
     * Search providers by filters.
     *
     * **Endpoint:** POST /api/v1/catalog/providers/search
     *
     * If geolocation is unavailable, defaults to Haifa coordinates (32.8, 35.0)
     * following the iOS stub pattern to get relevant providers.
     */
    suspend fun searchProviders(filters: SearchFilters): Result<ProviderListResponse> {
        return safeApiCall<ProviderListResponse> {
            // Default to Haifa coordinates when geo not available
            val lat = filters.latitude ?: HAIFA_LAT
            val lon = filters.longitude ?: HAIFA_LON

            val request =
                ProviderSearchRequest(
                    lat = lat,
                    lon = lon,
                    radiusKm = filters.radiusKm ?: DEFAULT_RADIUS_KM,
                    categoryId = filters.categoryIds.firstOrNull(),
                    sortBy =
                        when (filters.sortBy) {
                            com.aggregateservice.feature.catalog.domain.model.SearchFilters.SortBy.DISTANCE -> "distance"
                            com.aggregateservice.feature.catalog.domain.model.SearchFilters.SortBy.RATING -> "rating"
                            else -> "rating"
                        },
                    limit = filters.pageSize,
                    offset = filters.offset,
                )
            client.post("/api/v1/catalog/providers/search") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    private companion object {
        const val HAIFA_LAT = 32.8
        const val HAIFA_LON = 35.0
        const val DEFAULT_RADIUS_KM = 30.0
    }

    /**
     * Get provider by ID.
     *
     * **Endpoint:** GET /api/v1/catalog/providers/{id}
     */
    suspend fun getProviderById(providerId: String): Result<ProviderResponse> {
        return safeApiCall<ProviderResponse> {
            client.get("/api/v1/catalog/providers/$providerId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Composite: provider + services + favorite status in one request.
     *
     * **Endpoint:** GET /api/v1/catalog/providers/{id}/detail
     */
    suspend fun getProviderDetail(providerId: String): Result<ProviderDetailResponse> {
        return safeApiCall<ProviderDetailResponse> {
            client.get("/api/v1/catalog/providers/$providerId/detail") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Get list of categories.
     *
     * **Endpoint:** GET /api/v1/catalog/categories
     */
    suspend fun getCategories(parentId: String?): Result<CategoryListResponse> {
        return safeApiCall<CategoryListResponse> {
            client.get("/api/v1/catalog/categories") {
                contentType(ContentType.Application.Json)
                parentId?.let {
                    url { parameters.append("parentId", it.toString()) }
                }
            }
        }
    }

    /**
     * Get category by ID.
     *
     * **Endpoint:** GET /api/v1/catalog/categories/{id}
     */
    suspend fun getCategoryById(categoryId: String): Result<CategoryResponse> {
        return safeApiCall<CategoryResponse> {
            client.get("/api/v1/catalog/categories/$categoryId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Get provider services.
     *
     * **Endpoint:** GET /api/v1/catalog/providers/{providerId}/services
     */
    suspend fun getProviderServices(
        providerId: String,
        categoryId: String?,
    ): Result<List<PublicProviderServiceItemResponse>> {
        return safeApiCall<PublicProviderServicesResponse> {
            client.get("/api/v1/catalog/providers/$providerId/services") {
                contentType(ContentType.Application.Json)
                categoryId?.let {
                    url { parameters.append("categoryId", it.toString()) }
                }
            }
        }.mapCatching { response -> response.services }
    }

    /**
     * Get service by ID.
     *
     * **Endpoint:** GET /api/v1/catalog/services/{id}
     */
    suspend fun getServiceById(serviceId: String): Result<ServiceResponse> {
        return safeApiCall<ServiceResponse> {
            client.get("/api/v1/catalog/services/$serviceId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Search services.
     *
     * **Endpoint:** GET /api/v1/catalog/services/search
     */
    suspend fun searchServices(
        query: String,
        filters: SearchFilters,
    ): Result<List<ServiceResponse>> {
        return safeApiCall<List<ServiceResponse>> {
            client.get("/api/v1/catalog/services/search") {
                contentType(ContentType.Application.Json)
                url {
                    parameters.append("q", query)
                    filters.categoryIds.forEach { parameters.append("categoryIds", it) }
                    parameters.append("page", filters.page.toString())
                    parameters.append("pageSize", filters.pageSize.toString())
                }
            }
        }
    }
}
