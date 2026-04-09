package com.aggregateservice.core.favorites_api

interface FavoritesToggle {
    suspend fun isFavorite(providerId: String): Result<Boolean>

    suspend fun addFavorite(providerId: String): Result<Unit>

    suspend fun removeFavorite(providerId: String): Result<Unit>
}
