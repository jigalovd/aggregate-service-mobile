package com.aggregateservice.feature.services.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.services.data.dto.CreateServiceRequestDto
import com.aggregateservice.feature.services.data.dto.ServiceDto
import com.aggregateservice.feature.services.data.dto.UpdateServiceRequestDto
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
 * - GET    /api/v1/provider-services          - List all services
 * - POST   /api/v1/provider-services          - Create service
 * - GET    /api/v1/provider-services/{id}     - Get service details
 * - PATCH  /api/v1/provider-services/{id}     - Update service
 * - DELETE /api/v1/provider-services/{id}     - Delete service
 *
 * @property client HTTP client (Ktor)
 */
class ServicesApiService(
    private val client: HttpClient,
) {
    /**
     * Retrieves all services for the authenticated provider.
     *
     * **Endpoint:** GET /api/v1/provider-services
     */
    suspend fun getServices(): Result<List<ServiceDto>> {
        return safeApiCall<List<ServiceDto>> {
            client.get("/api/v1/provider-services") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Retrieves a specific service by ID.
     *
     * **Endpoint:** GET /api/v1/provider-services/{id}
     */
    suspend fun getServiceById(id: String): Result<ServiceDto> {
        return safeApiCall<ServiceDto> {
            client.get("/api/v1/provider-services/$id") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Creates a new service.
     *
     * **Endpoint:** POST /api/v1/provider-services
     */
    suspend fun createService(request: CreateServiceRequestDto): Result<ServiceDto> {
        return safeApiCall<ServiceDto> {
            client.post("/api/v1/provider-services") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Updates an existing service.
     *
     * **Endpoint:** PATCH /api/v1/provider-services/{id}
     */
    suspend fun updateService(id: String, request: UpdateServiceRequestDto): Result<ServiceDto> {
        return safeApiCall<ServiceDto> {
            client.patch("/api/v1/provider-services/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Deletes a service.
     *
     * **Endpoint:** DELETE /api/v1/provider-services/{id}
     */
    suspend fun deleteService(id: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.delete("/api/v1/provider-services/$id") {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
