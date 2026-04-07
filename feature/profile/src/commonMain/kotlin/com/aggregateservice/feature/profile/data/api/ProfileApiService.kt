package com.aggregateservice.feature.profile.data.api

import com.aggregateservice.core.network.safeApiCall
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
 * **Auth:** Ktor Auth Plugin handles Authorization header automatically
 *
 * @property client HTTP client (Ktor)
 */
class ProfileApiService(
    private val client: HttpClient,
) {
    /**
     * Retrieves the current user's profile.
     *
     * **Endpoint:** GET /api/v1/profiles/me
     * **Auth:** Requires valid access token (handled by Ktor Auth Plugin)
     */
    suspend fun getProfile(): Result<ProfileDto> {
        return safeApiCall<ProfileDto> {
            client.get("/api/v1/profiles/me") {
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Updates the current user's profile.
     *
     * **Endpoint:** PATCH /api/v1/profiles/me
     * **Auth:** Requires valid access token (handled by Ktor Auth Plugin)
     */
    suspend fun updateProfile(request: UpdateProfileRequestDto): Result<ProfileDto> {
        return safeApiCall<ProfileDto> {
            client.patch("/api/v1/profiles/me") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}