package com.aggregateservice.feature.favorites.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteCheckResponseDto(
    @SerialName("is_favorite") val isFavorite: Boolean,
    @SerialName("favorite_id") val favoriteId: String? = null,
)
