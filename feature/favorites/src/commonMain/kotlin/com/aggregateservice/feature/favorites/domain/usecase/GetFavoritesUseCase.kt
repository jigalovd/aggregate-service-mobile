package com.aggregateservice.feature.favorites.domain.usecase

import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository

/**
 * UseCase for retrieving all favorites for the authenticated user.
 *
 * @property repository Favorites repository
 */
class GetFavoritesUseCase(
    private val repository: FavoritesRepository,
) {
    /**
     * Retrieves all favorites.
     *
     * @return Result containing list of favorites or an error
     */
    suspend operator fun invoke(): Result<List<Favorite>> {
        return repository.getFavorites()
    }
}
