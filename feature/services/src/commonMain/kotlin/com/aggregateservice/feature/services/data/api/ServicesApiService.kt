package com.aggregateservice.feature.services.data.api

import com.aggregateservice.core.api.models.ProviderServiceCreateRequest
import com.aggregateservice.core.api.models.ProviderServiceListResponse
import com.aggregateservice.core.api.models.ProviderServiceResponse
import com.aggregateservice.core.api.models.ProviderServiceUpdateRequest
import com.aggregateservice.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API service for provider services management.
 *
 * **Endpoints:**
 * - GET    /api/v1/providers/services          - List all services (auth)
 * - POST   /api/v1/providers/services          - Create service (auth)
 * - GET    /api/v1/providers/services/{id}     - Get service details (auth)
 * - PATCH  /api/v1/providers/services/{id}     - Update service (auth)
 * - DELETE /api/v1/providers/services/{id}     - Delete service (auth)
 *
 * **Auth:** Ktor Auth Plugin handles Authorization header automatically
 *
 * @property client HTTP client (Ktor)
 */
class ServicesApiService(
    private val client: HttpClient,
) {
    /**
     * Retrieves all services for the authenticated provider.
     *
     * **Endpoint:** GET /api/v1/providers/services
     */
    suspend fun getServices(): Result<List<ProviderServiceResponse>> {
        return safeApiCall<ProviderServiceListResponse> {
            client.get("/api/v1/providers/services") {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching { response -> response.services }
    }

    /**
     * Retrieves a specific service by ID.
     *
     * **Endpoint:** GET /api/v1/providers/services/{id}
     */
    suspend fun getServiceById(id: String): Result<ProviderServiceResponse> {
        return safeApiCall<ProviderServiceResponse> {
            client.get("/api/v1/providers/services/$id") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Creates a new service.
     *
     * **Endpoint:** POST /api/v1/providers/services
     */
    suspend fun createService(request: ProviderServiceCreateRequest): Result<ProviderServiceResponse> {
        return safeApiCall<ProviderServiceResponse> {
            client.post("/api/v1/providers/services") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Updates an existing service.
     *
     * **Endpoint:** PATCH /api/v1/providers/services/{id}
     */
    suspend fun updateService(id: String, request: ProviderServiceUpdateRequest): Result<ProviderServiceResponse> {
        return safeApiCall<ProviderServiceResponse> {
            client.patch("/api/v1/providers/services/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Deletes a service.
     *
     * **Endpoint:** DELETE /api/v1/providers/services/{id}
     */
    suspend fun deleteService(id: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.delete("/api/v1/providers/services/$id") {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
