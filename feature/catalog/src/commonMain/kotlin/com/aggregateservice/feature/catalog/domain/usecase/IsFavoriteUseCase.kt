package com.aggregateservice.feature.catalog.domain.usecase

import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository

/**
 * UseCase for checking if a provider is in favorites.
 */
class IsFavoriteUseCase(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(providerId: String): Result<Boolean> {
        return repository.isFavorite(providerId)
    }
}
