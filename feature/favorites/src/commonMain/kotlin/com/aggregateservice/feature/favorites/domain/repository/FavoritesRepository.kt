package com.aggregateservice.feature.favorites.domain.repository

import com.aggregateservice.feature.favorites.domain.model.Favorite

/**
 * Repository interface for favorites management.
 * Provides operations for managing user's favorite providers.
 */
interface FavoritesRepository {
    /**
     * Retrieves all favorites for the authenticated user.
     *
     * @return Result containing list of favorites or an error
     */
    suspend fun getFavorites(): Result<List<Favorite>>

    /**
     * Adds a provider to favorites.
     *
     * @param providerId The provider ID to add
     * @return Result indicating success or an error
     */
    suspend fun addFavorite(providerId: String): Result<Unit>

    /**
     * Removes a provider from favorites.
     *
     * @param providerId The provider ID to remove
     * @return Result indicating success or an error
     */
    suspend fun removeFavorite(providerId: String): Result<Unit>

    /**
     * Checks if a provider is in favorites.
     *
     * @param providerId The provider ID to check
     * @return Result containing boolean or an error
     */
    suspend fun isFavorite(providerId: String): Result<Boolean>
}
