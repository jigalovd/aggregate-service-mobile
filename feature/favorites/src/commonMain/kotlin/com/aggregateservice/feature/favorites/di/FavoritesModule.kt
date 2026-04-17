package com.aggregateservice.feature.favorites.di

import co.touchlab.kermit.Logger
import com.aggregateservice.core.favoritesApi.FavoritesToggle
import com.aggregateservice.feature.favorites.data.adapter.FavoritesToggleAdapter
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
val favoritesModule =
    module {
        // API Service (Ktor Auth Plugin handles auth automatically)
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

        // FavoritesToggle adapter (for cross-feature use via core:favorites-api)
        single<FavoritesToggle> {
            FavoritesToggleAdapter(
                isFavoriteUseCase = get(),
                addFavoriteUseCase = get(),
                removeFavoriteUseCase = get(),
            )
        }

        // ScreenModels (Presentation layer)
        factory { FavoritesScreenModel(get(), get(), Logger.withTag("Favorites")) }
    }
