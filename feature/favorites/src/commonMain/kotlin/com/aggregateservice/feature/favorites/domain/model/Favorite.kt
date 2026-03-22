package com.aggregateservice.feature.favorites.domain.model

import kotlinx.datetime.Instant

/**
 * Domain entity representing a favorite provider.
 *
 * **Note:** Domain models must NOT import Compose/Android dependencies.
 * Stability is ensured by data class immutability.
 *
 * @property providerId Unique identifier of the provider
 * @property businessName Name of the provider's business
 * @property logoUrl URL of the provider's logo (optional)
 * @property rating Average rating of the provider
 * @property reviewCount Number of reviews
 * @property address Formatted address of the provider
 * @property addedAt Timestamp when added to favorites
 */
data class Favorite(
    val providerId: String,
    val businessName: String,
    val logoUrl: String?,
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val addedAt: Instant,
) {
    /**
     * Formatted rating as "X.X".
     */
    val formattedRating: String
        get() = "%.1f".format(rating)

    /**
     * Display text for review count.
     */
    val reviewCountText: String
        get() = "$reviewCount reviews"
}
