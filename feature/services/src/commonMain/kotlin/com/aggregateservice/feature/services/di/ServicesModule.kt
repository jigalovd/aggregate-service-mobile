package com.aggregateservice.feature.services.di

import com.aggregateservice.feature.services.data.api.ServicesApiService
import com.aggregateservice.feature.services.data.repository.ServicesRepositoryImpl
import com.aggregateservice.feature.services.domain.repository.ServicesRepository
import com.aggregateservice.feature.services.domain.usecase.CreateServiceUseCase
import com.aggregateservice.feature.services.domain.usecase.DeleteServiceUseCase
import com.aggregateservice.feature.services.domain.usecase.GetServiceByIdUseCase
import com.aggregateservice.feature.services.domain.usecase.GetServicesUseCase
import com.aggregateservice.feature.services.domain.usecase.UpdateServiceUseCase
import com.aggregateservice.feature.services.presentation.screenmodel.ServiceFormScreenModel
import com.aggregateservice.feature.services.presentation.screenmodel.ServicesListScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Services feature DI module.
 *
 * Provides dependencies for feature:services:
 * - Repository (implementation)
 * - UseCases (domain layer)
 * - ScreenModels (presentation layer)
 */
val servicesModule = module {
    // API Service (Ktor Auth Plugin handles auth automatically)
    single { ServicesApiService(get()) }

    // Repository
    single<ServicesRepository> {
        ServicesRepositoryImpl(apiService = get())
    }

    // UseCases (Domain layer)
    factoryOf(::GetServicesUseCase)
    factoryOf(::GetServiceByIdUseCase)
    factoryOf(::CreateServiceUseCase)
    factoryOf(::UpdateServiceUseCase)
    factoryOf(::DeleteServiceUseCase)

    // ScreenModels (Presentation layer)
    factoryOf(::ServicesListScreenModel)
    factoryOf(::ServiceFormScreenModel)
}
