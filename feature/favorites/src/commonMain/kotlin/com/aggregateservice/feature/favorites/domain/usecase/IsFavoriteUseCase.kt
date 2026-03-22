package com.aggregateservice.feature.favorites.domain.usecase

import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository

/**
 * UseCase for checking if a provider is in favorites.
 *
 * @property repository Favorites repository
 */
class IsFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    /**
     * Checks if a provider is in favorites.
     *
     * @param providerId The provider ID to check
     * @return Result containing boolean or an error
     */
    suspend operator fun invoke(providerId: String): Result<Boolean> {
        return repository.isFavorite(providerId)
    }
}
