package com.aggregateservice.feature.profile.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.core.network.withAuth
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.profile.data.dto.ProfileDto
import com.aggregateservice.feature.profile.data.dto.UpdateProfileRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API service for user profile management.
 *
 * **Endpoints:**
 * - GET   /api/v1/profiles/me  - Get current user's profile (authenticated)
 * - PATCH /api/v1/profiles/me  - Update current user's profile (authenticated)
 *
 * @property client HTTP client (Ktor)
 * @property tokenStorage Token storage for auth header injection
 */
class ProfileApiService(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) {
    /**
     * Retrieves the current user's profile.
     *
     * **Endpoint:** GET /api/v1/profiles/me
     * **Auth:** Requires valid access token
     */
    suspend fun getProfile(): Result<ProfileDto> {
        return safeApiCall<ProfileDto> {
            client.get("/api/v1/profiles/me") {
                contentType(ContentType.Application.Json)
                withAuth(tokenStorage)
            }
        }
    }

    /**
     * Updates the current user's profile.
     *
     * **Endpoint:** PATCH /api/v1/profiles/me
     * **Auth:** Requires valid access token
     */
    suspend fun updateProfile(request: UpdateProfileRequestDto): Result<ProfileDto> {
        return safeApiCall<ProfileDto> {
            client.patch("/api/v1/profiles/me") {
                contentType(ContentType.Application.Json)
                withAuth(tokenStorage)
                setBody(request)
            }
        }
    }
}
