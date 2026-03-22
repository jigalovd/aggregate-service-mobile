package com.aggregateservice.feature.reviews.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for ReviewStats from API.
 */
@Serializable
data class ReviewStatsDto(
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("average_rating")
    val averageRating: Double,
    @SerialName("total_reviews")
    val totalReviews: Int,
    @SerialName("rating_distribution")
    val ratingDistribution: Map<String, Int>, // JSON keys are strings
)

/**
 * Response DTO for can-review check.
 */
@Serializable
data class CanReviewResponseDto(
    @SerialName("can_review")
    val canReview: Boolean,
)
