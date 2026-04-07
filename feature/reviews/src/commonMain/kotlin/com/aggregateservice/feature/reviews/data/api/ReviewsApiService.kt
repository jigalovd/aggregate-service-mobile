package com.aggregateservice.feature.reviews.data.api

import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.feature.reviews.data.dto.CanReviewResponseDto
import com.aggregateservice.feature.reviews.data.dto.CreateReviewRequest
import com.aggregateservice.feature.reviews.data.dto.ReviewDto
import com.aggregateservice.feature.reviews.data.dto.ReviewStatsDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.builtins.ListSerializer

/**
 * API service for reviews endpoints.
 *
 * **Endpoints:**
 * - GET    /api/v1/reviews/provider/{providerId}  - Get provider reviews (public)
 * - GET    /api/v1/reviews/stats/provider/{providerId} - Get review stats (public)
 * - GET    /api/v1/bookings/{bookingId}/can-review - Check if can review (auth)
 * - POST   /api/v1/reviews - Create review (auth)
 *
 * **Auth:** Ktor Auth Plugin handles Authorization header automatically
 *
 * @property client HTTP client (Ktor)
 */
class ReviewsApiService(
    private val client: HttpClient,
) {
    suspend fun getProviderReviews(
        providerId: String,
        page: Int,
        pageSize: Int,
    ): Result<List<ReviewDto>> = safeApiCall {
        client.get("$REVIEWS_PATH/provider/$providerId") {
            contentType(ContentType.Application.Json)
            parameter("page", page)
            parameter("page_size", pageSize)
        }
    }

    suspend fun getReviewStats(providerId: String): Result<ReviewStatsDto> = safeApiCall {
        client.get("$REVIEWS_PATH/stats/provider/$providerId") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun canReviewBooking(bookingId: String): Result<CanReviewResponseDto> = safeApiCall {
        client.get("$BOOKINGS_PATH/$bookingId/can-review") {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun createReview(request: CreateReviewRequest): Result<ReviewDto> = safeApiCall {
        client.post(REVIEWS_PATH) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    companion object {
        private const val REVIEWS_PATH = "/api/v1/reviews"
        private const val BOOKINGS_PATH = "/api/v1/bookings"
    }
}