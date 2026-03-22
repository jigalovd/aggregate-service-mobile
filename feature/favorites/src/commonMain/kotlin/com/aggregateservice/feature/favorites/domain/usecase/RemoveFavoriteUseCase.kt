package com.aggregateservice.feature.favorites.domain.usecase

import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository

/**
 * UseCase for removing a provider from favorites.
 *
 * @property repository Favorites repository
 */
class RemoveFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    /**
     * Removes a provider from favorites.
     *
     * @param providerId The provider ID to remove
     * @return Result indicating success or an error
     */
    suspend operator fun invoke(providerId: String): Result<Unit> {
        return repository.removeFavorite(providerId)
    }
}
