package com.aggregateservice.feature.favorites.data.repository

import com.aggregateservice.feature.favorites.data.api.FavoritesApiService
import com.aggregateservice.feature.favorites.data.mapper.FavoriteMapper
import com.aggregateservice.feature.favorites.domain.model.Favorite
import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository

/**
 * Implementation of FavoritesRepository.
 *
 * **Architecture:**
 * - Data layer implements Domain layer interface
 * - Uses FavoritesApiService for network requests
 * - Uses FavoriteMapper for DTO -> Domain conversion
 *
 * @property apiService API service for favorites
 */
class FavoritesRepositoryImpl(
    private val apiService: FavoritesApiService,
) : FavoritesRepository {

    override suspend fun getFavorites(): Result<List<Favorite>> {
        return apiService.getFavorites().fold(
            onSuccess = { dtos -> Result.success(FavoriteMapper.toDomain(dtos)) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun addFavorite(providerId: String): Result<Unit> {
        return apiService.addFavorite(providerId)
    }

    override suspend fun removeFavorite(providerId: String): Result<Unit> {
        return apiService.removeFavorite(providerId)
    }

    override suspend fun isFavorite(providerId: String): Result<Boolean> {
        return apiService.isFavorite(providerId)
    }
}
