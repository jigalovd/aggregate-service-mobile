package com.aggregateservice.feature.favorites.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.favorites.data.dto.FavoriteCheckResponseDto
import com.aggregateservice.feature.favorites.data.dto.FavoritesListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API service for favorites management.
 *
 * **Endpoints:**
 * - GET    /api/v1/catalog/favorites                  - List all favorites (auth)
 * - POST   /api/v1/catalog/favorites/{providerId}     - Add to favorites (auth)
 * - DELETE /api/v1/catalog/favorites/{providerId}     - Remove from favorites (auth)
 * - GET    /api/v1/catalog/favorites/{providerId}/check - Check if favorite (auth)
 *
 * **Auth:** Ktor Auth Plugin handles Authorization header automatically
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
    suspend fun getFavorites(): Result<FavoritesListResponseDto> {
        return safeApiCall<FavoritesListResponseDto> {
            client.get("/api/v1/catalog/favorites") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Adds a provider to favorites.
     *
     * **Endpoint:** POST /api/v1/catalog/favorites
     */
    suspend fun addFavorite(providerId: String): Result<Unit> {
        return safeApiCall<Unit> {
            client.post("/api/v1/catalog/favorites") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("provider_id" to providerId))
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
        return safeApiCall<FavoriteCheckResponseDto> {
            client.get("/api/v1/catalog/favorites/$providerId/check") {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching { response -> response.isFavorite }
    }
}
