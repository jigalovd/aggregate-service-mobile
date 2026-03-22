package com.aggregateservice.feature.favorites.domain.usecase

import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository

/**
 * UseCase for adding a provider to favorites.
 *
 * @property repository Favorites repository
 */
class AddFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    /**
     * Adds a provider to favorites.
     *
     * @param providerId The provider ID to add
     * @return Result indicating success or an error
     */
    suspend operator fun invoke(providerId: String): Result<Unit> {
        return repository.addFavorite(providerId)
    }
}
