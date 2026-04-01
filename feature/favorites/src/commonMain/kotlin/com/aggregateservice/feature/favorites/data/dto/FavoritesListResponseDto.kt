package com.aggregateservice.feature.favorites.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FavoritesListResponseDto(
    val favorites: List<FavoriteDto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)
