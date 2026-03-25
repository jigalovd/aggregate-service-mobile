package com.aggregateservice.feature.favorites.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.favorites.data.dto.FavoriteDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API service for favorites management.
 *
 * **Endpoints:**
 * - GET    /api/v1/catalog/favorites                  - List all favorites
 * - POST   /api/v1/catalog/favorites/{providerId}     - Add to favorites
 * - DELETE /api/v1/catalog/favorites/{providerId}     - Remove from favorites
 * - GET    /api/v1/catalog/favorites/{providerId}/check - Check if favorite
 *
 * @property client HTTP client (Ktor)
 */
class FavoritesApiService(
    private val client: HttpClient,
) {
    /**
     * Retrieves all favorites for the authenticated user.
     *
     * **Endpoint:** GET /api/v1/catalog/favorites
     */
    suspend fun getFavorites(): Result<List<FavoriteDto>> {
        return safeApiCall<List<FavoriteDto>> {
            client.get("/api/v1/catalog/favorites") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Adds a provider to favorites.
     *
     * **Endpoint:** POST /api/v1/catalog/favorites/{providerId}
     */
    suspend fun addFavorite(providerId: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.post("/api/v1/catalog/favorites/$providerId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Removes a provider from favorites.
     *
     * **Endpoint:** DELETE /api/v1/catalog/favorites/{providerId}
     */
    suspend fun removeFavorite(providerId: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.delete("/api/v1/catalog/favorites/$providerId") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Checks if a provider is in favorites.
     *
     * **Endpoint:** GET /api/v1/catalog/favorites/{providerId}/check
     */
    suspend fun isFavorite(providerId: String): Result<Boolean> {
        return safeApiCall<Boolean> {
            client.get("/api/v1/catalog/favorites/$providerId/check") {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
