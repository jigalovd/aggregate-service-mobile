package com.aggregateservice.feature.favorites.data.adapter

import com.aggregateservice.core.favoritesApi.FavoritesToggle
import com.aggregateservice.feature.favorites.domain.usecase.AddFavoriteUseCase
import com.aggregateservice.feature.favorites.domain.usecase.IsFavoriteUseCase
import com.aggregateservice.feature.favorites.domain.usecase.RemoveFavoriteUseCase

class FavoritesToggleAdapter(
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
) : FavoritesToggle {
    override suspend fun isFavorite(providerId: String): Result<Boolean> =
        isFavoriteUseCase.invoke(providerId)

    override suspend fun addFavorite(providerId: String): Result<Unit> =
        addFavoriteUseCase.invoke(providerId)

    override suspend fun removeFavorite(providerId: String): Result<Unit> =
        removeFavoriteUseCase.invoke(providerId)
}
