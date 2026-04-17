package com.aggregateservice.core.favoritesApi

interface FavoritesToggle {
    suspend fun isFavorite(providerId: String): Result<Boolean>

    suspend fun addFavorite(providerId: String): Result<Unit>

    suspend fun removeFavorite(providerId: String): Result<Unit>
}
