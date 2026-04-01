package com.aggregateservice.feature.favorites.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("provider_id") val providerId: String,
    val provider: ProviderDto,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ProviderDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("rating_cached") val ratingCached: Double = 0.0,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    val address: String? = null,
    val bio: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
)
