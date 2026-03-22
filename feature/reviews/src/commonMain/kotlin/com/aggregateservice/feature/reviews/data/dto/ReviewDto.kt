package com.aggregateservice.feature.reviews.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Review from API.
 */
@Serializable
data class ReviewDto(
    @SerialName("id")
    val id: String,
    @SerialName("booking_id")
    val bookingId: String,
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("provider_name")
    val providerName: String,
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_name")
    val clientName: String,
    @SerialName("rating")
    val rating: Int,
    @SerialName("comment")
    val comment: String?,
    @SerialName("provider_reply")
    val providerReply: ProviderReplyDto?,
    @SerialName("created_at")
    val createdAt: Instant,
)

@Serializable
data class ProviderReplyDto(
    @SerialName("text")
    val text: String,
    @SerialName("created_at")
    val createdAt: Instant,
)

/**
 * Request DTO for creating a review.
 */
@Serializable
data class CreateReviewRequest(
    @SerialName("booking_id")
    val bookingId: String,
    @SerialName("rating")
    val rating: Int,
    @SerialName("comment")
    val comment: String?,
)
