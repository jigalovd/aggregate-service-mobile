package com.aggregateservice.feature.provider.onboarding.data.api

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API Service for provider onboarding.
 *
 * Uses HttpClient for backend API communication.
 *
 * **Endpoints:**
 * - POST /api/v1/providers/onboarding — submit provider onboarding data
 */
class ProviderOnboardingApiService(
    private val client: HttpClient,
) {
    /**
     * Submits provider onboarding data.
     *
     * @param request Onboarding data including business info, location, and services
     * @return Result with ProviderOnboardingResponse on success or error
     */
    suspend fun submitOnboarding(request: ProviderOnboardingRequest): Result<ProviderOnboardingResponse> {
        return safeApiCall {
            client.post("/api/v1/providers/onboarding") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}

/**
 * Request body for provider onboarding submission.
 */
@Serializable
data class ProviderOnboardingRequest(
    @SerialName("business_name")
    val businessName: String,
    @SerialName("bio")
    val bio: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("address")
    val address: String,
    @SerialName("service_radius_km")
    val serviceRadiusKm: Float,
    @SerialName("category_ids")
    val categoryIds: List<String>,
)

/**
 * Response body for successful provider onboarding.
 *
 * @property message Server message (e.g., "Onboarding successful")
 * @property accessToken New access token for authenticated PROVIDER session
 */
@Serializable
data class ProviderOnboardingResponse(
    @SerialName("message")
    val message: String,
    @SerialName("access_token")
    val accessToken: String,
)