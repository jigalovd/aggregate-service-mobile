package com.aggregateservice.feature.catalog.di

import co.touchlab.kermit.Logger
import com.aggregateservice.core.location.LocationProviderFactory
import com.aggregateservice.core.navigation.CatalogNavigator
import com.aggregateservice.feature.catalog.data.api.CatalogApiService
import com.aggregateservice.feature.catalog.data.repository.CatalogRepositoryImpl
import com.aggregateservice.feature.catalog.domain.repository.CatalogRepository
import com.aggregateservice.feature.catalog.domain.usecase.GetCategoriesUseCase
import com.aggregateservice.feature.catalog.domain.usecase.GetProviderDetailsUseCase
import com.aggregateservice.feature.catalog.domain.usecase.GetProviderServicesUseCase
import com.aggregateservice.feature.catalog.domain.usecase.SearchProvidersUseCase
import com.aggregateservice.feature.catalog.navigation.CatalogNavigatorImpl
import com.aggregateservice.feature.catalog.presentation.screenmodel.CatalogScreenModel
import com.aggregateservice.feature.catalog.presentation.screenmodel.ProviderDetailScreenModel
import com.aggregateservice.feature.catalog.presentation.screenmodel.SearchScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Catalog feature DI модуль.
 *
 * Предоставляет зависимости для feature:catalog:
 * - Repository (implementation)
 * - UseCases (domain layer)
 * - ScreenModels (presentation layer)
 *
 * **Dependency Graph:**
 * ```
 * CatalogScreenModel
 *   ├── SearchProvidersUseCase
 *   │     └── CatalogRepository
 *   │           └── CatalogApiService (HttpClient)
 *   └── GetCategoriesUseCase
 *         └── CatalogRepository
 *
 * ProviderDetailScreenModel
 *   ├── GetProviderDetailsUseCase
 *   │     └── CatalogRepository
 *   └── GetProviderServicesUseCase
 *         └── CatalogRepository
 * ```
 *
 * **Important:** Все зависимости разрешаются через Koin DI container.
 */
val catalogModule =
    module {
        // API Service
        single { CatalogApiService(get()) }

        // Repository
        single<CatalogRepository> {
            CatalogRepositoryImpl(
                apiService = get(),
            )
        }

        // UseCases (Domain layer)
        factoryOf(::SearchProvidersUseCase)
        factoryOf(::GetProviderDetailsUseCase)
        factoryOf(::GetProviderServicesUseCase)
        factoryOf(::GetCategoriesUseCase)

        // ScreenModels (Presentation layer)
        factory { CatalogScreenModel(get(), get(), get(), Logger.withTag("Catalog")) }
        factory {
            ProviderDetailScreenModel(
                getProviderDetailsUseCase = get(),
                getProviderServicesUseCase = get(),
                favoritesToggle = get(),
                logger = Logger.withTag("Catalog"),
            )
        }
        factory { SearchScreenModel(get(), get(), Logger.withTag("Catalog")) }

        // Navigation
        single<CatalogNavigator> { CatalogNavigatorImpl() }

        // LocationProvider singleton per D-06
        single { LocationProviderFactory.create() }
    }
