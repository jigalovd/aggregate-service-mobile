package com.aggregateservice.feature.reviews.domain.model

import kotlinx.datetime.Instant

/**
 * Domain entity representing a review for a service provider.
 *
 * **Note:** Domain models must NOT import Compose/Android dependencies.
 * Stability is ensured by data class immutability.
 *
 * @property id Unique identifier of the review
 * @property bookingId ID of the booking this review is for (prevents fake reviews)
 * @property providerId ID of the service provider
 * @property providerName Name of the service provider
 * @property clientId ID of the client who wrote the review
 * @property clientName Name of the client
 * @property rating Rating from 1 to 5
 * @property comment Optional comment text
 * @property providerReply Optional reply from the provider
 * @property createdAt When the review was created
 */
data class Review(
    val id: String,
    val bookingId: String,
    val providerId: String,
    val providerName: String,
    val clientId: String,
    val clientName: String,
    val rating: Int,
    val comment: String?,
    val providerReply: ProviderReply?,
    val createdAt: Instant,
) {
    init {
        require(rating in RATING_MIN..RATING_MAX) {
            "Rating must be between $RATING_MIN and $RATING_MAX"
        }
    }

    /**
     * Formatted rating as "X.X".
     */
    val formattedRating: String
        get() = "%.1f".format(rating.toDouble())

    /**
     * Whether this review has a comment.
     */
    val hasComment: Boolean
        get() = !comment.isNullOrBlank()

    /**
     * Whether the provider has replied to this review.
     */
    val hasProviderReply: Boolean
        get() = providerReply != null

    companion object {
        const val RATING_MIN = 1
        const val RATING_MAX = 5
    }
}

/**
 * Provider's reply to a review.
 *
 * @property text The reply text
 * @property createdAt When the reply was created
 */
data class ProviderReply(
    val text: String,
    val createdAt: Instant,
)
