package com.aggregateservice.feature.booking.di

import co.touchlab.kermit.Logger
import com.aggregateservice.core.navigation.BookingNavigator
import com.aggregateservice.feature.booking.data.api.BookingApiService
import com.aggregateservice.feature.booking.data.repository.BookingRepositoryImpl
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import com.aggregateservice.feature.booking.domain.usecase.CancelBookingUseCase
import com.aggregateservice.feature.booking.domain.usecase.CreateBookingUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetAvailableSlotsUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetBookingByIdUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetBookingServicesUseCase
import com.aggregateservice.feature.booking.domain.usecase.GetClientBookingsUseCase
import com.aggregateservice.feature.booking.domain.usecase.RescheduleBookingUseCase
import com.aggregateservice.feature.booking.navigation.BookingNavigatorImpl
import com.aggregateservice.feature.booking.presentation.screenmodel.BookingConfirmationScreenModel
import com.aggregateservice.feature.booking.presentation.screenmodel.BookingHistoryScreenModel
import com.aggregateservice.feature.booking.presentation.screenmodel.SelectDateTimeScreenModel
import com.aggregateservice.feature.booking.presentation.screenmodel.SelectServiceScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Booking feature DI модуль.
 *
 * Предоставляет зависимости для feature:booking:
 * - BookingNavigator (для навигации из других фич)
 * - Repository (implementation)
 * - UseCases (domain layer)
 * - ScreenModels (presentation layer)
 */
val bookingModule =
    module {
        // Navigator for cross-feature navigation
        single<BookingNavigator> { BookingNavigatorImpl() }

        // API Service (Ktor Auth Plugin handles auth automatically)
        single { BookingApiService(get()) }

        // Repository
        single<BookingRepository> {
            BookingRepositoryImpl(apiService = get())
        }

        // UseCases (Domain layer)
        factoryOf(::CreateBookingUseCase)
        factoryOf(::CancelBookingUseCase)
        factoryOf(::RescheduleBookingUseCase)
        factoryOf(::GetBookingByIdUseCase)
        factoryOf(::GetClientBookingsUseCase)
        factoryOf(::GetAvailableSlotsUseCase)
        factoryOf(::GetBookingServicesUseCase)

        // ScreenModels (Presentation layer)
        factory<Logger> { Logger.withTag("Booking") }
        factoryOf(::SelectServiceScreenModel)
        factoryOf(::SelectDateTimeScreenModel)
        factoryOf(::BookingConfirmationScreenModel)
        factoryOf(::BookingHistoryScreenModel)
    }
