package com.aggregateservice.feature.favorites.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for favorite provider from API response.
 *
 * @property providerId Unique identifier of the provider
 * @property businessName Name of the provider's business
 * @property logoUrl URL of the provider's logo (optional)
 * @property rating Average rating of the provider
 * @property reviewCount Number of reviews
 * @property address Formatted address of the provider
 * @property addedAt Timestamp when added to favorites
 */
@Serializable
data class FavoriteDto(
    @SerialName("providerId")
    val providerId: String,
    @SerialName("businessName")
    val businessName: String,
    @SerialName("logoUrl")
    val logoUrl: String?,
    @SerialName("rating")
    val rating: Double,
    @SerialName("reviewCount")
    val reviewCount: Int,
    @SerialName("address")
    val address: String,
    @SerialName("addedAt")
    val addedAt: Instant,
)
