package com.aggregateservice.feature.auth.di

import com.aggregateservice.feature.auth.data.remote.AuthApiService
import com.aggregateservice.feature.auth.data.repository.AuthRepositoryImpl
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import com.aggregateservice.feature.auth.domain.usecase.LoginUseCase
import com.aggregateservice.feature.auth.domain.usecase.LogoutUseCase
import com.aggregateservice.feature.auth.domain.usecase.RegisterUseCase
import com.aggregateservice.feature.auth.presentation.viewmodel.AuthViewModel
import io.ktor.client.*
import org.koin.core.module.dsl.factory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single { AuthApiService(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get(), get()) }
    factory { RegisterUseCase(get()) }
    factory { LogoutUseCase(get(), get()) }

    factory { AuthViewModel(get(), get(), get()) }
}
