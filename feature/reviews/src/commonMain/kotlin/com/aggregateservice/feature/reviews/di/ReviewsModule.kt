package com.aggregateservice.feature.reviews.di

import com.aggregateservice.feature.reviews.data.api.ReviewsApiService
import com.aggregateservice.feature.reviews.data.repository.ReviewsRepositoryImpl
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository
import com.aggregateservice.feature.reviews.domain.usecase.CanReviewBookingUseCase
import com.aggregateservice.feature.reviews.domain.usecase.CreateReviewUseCase
import com.aggregateservice.feature.reviews.domain.usecase.GetProviderReviewsUseCase
import com.aggregateservice.feature.reviews.domain.usecase.GetReviewStatsUseCase
import com.aggregateservice.feature.reviews.presentation.screenmodel.ReviewsScreenModel
import com.aggregateservice.feature.reviews.presentation.screenmodel.WriteReviewScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * DI module for the Reviews feature.
 *
 * Provides:
 * - API Service
 * - Repository (implementation)
 * - UseCases (domain layer)
 * - ScreenModels (presentation layer)
 */
val reviewsModule = module {
    // API Service (Ktor Auth Plugin handles auth automatically)
    single { ReviewsApiService(get()) }

    // Repository
    single<ReviewsRepository> {
        ReviewsRepositoryImpl(apiService = get())
    }

    // UseCases (Domain layer)
    factoryOf(::GetProviderReviewsUseCase)
    factoryOf(::GetReviewStatsUseCase)
    factoryOf(::CanReviewBookingUseCase)
    factoryOf(::CreateReviewUseCase)

    // ScreenModels (Presentation layer)
    factoryOf(::ReviewsScreenModel)
    factoryOf(::WriteReviewScreenModel)
}
