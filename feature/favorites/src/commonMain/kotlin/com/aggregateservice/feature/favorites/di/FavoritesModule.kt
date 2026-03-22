package com.aggregateservice.feature.favorites.di

import com.aggregateservice.feature.favorites.data.api.FavoritesApiService
import com.aggregateservice.feature.favorites.data.repository.FavoritesRepositoryImpl
import com.aggregateservice.feature.favorites.domain.repository.FavoritesRepository
import com.aggregateservice.feature.favorites.domain.usecase.AddFavoriteUseCase
import com.aggregateservice.feature.favorites.domain.usecase.GetFavoritesUseCase
import com.aggregateservice.feature.favorites.domain.usecase.IsFavoriteUseCase
import com.aggregateservice.feature.favorites.domain.usecase.RemoveFavoriteUseCase
import com.aggregateservice.feature.favorites.presentation.screenmodel.FavoritesScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Favorites feature DI module.
 *
 * Provides dependencies for feature:favorites:
 * - Repository (implementation)
 * - UseCases (domain layer)
 * - ScreenModels (presentation layer)
 */
val favoritesModule = module {
    // API Service
    single { FavoritesApiService(get()) }

    // Repository
    single<FavoritesRepository> {
        FavoritesRepositoryImpl(apiService = get())
    }

    // UseCases (Domain layer)
    factoryOf(::GetFavoritesUseCase)
    factoryOf(::AddFavoriteUseCase)
    factoryOf(::RemoveFavoriteUseCase)
    factoryOf(::IsFavoriteUseCase)

    // ScreenModels (Presentation layer)
    factoryOf(::FavoritesScreenModel)
}
