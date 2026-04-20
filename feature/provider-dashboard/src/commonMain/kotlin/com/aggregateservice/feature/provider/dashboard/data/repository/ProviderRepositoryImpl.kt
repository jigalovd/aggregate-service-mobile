package com.aggregateservice.feature.provider.dashboard.data.repository

import co.touchlab.kermit.Logger
import com.aggregateservice.feature.provider.dashboard.data.api.ProviderDashboardApiService
import com.aggregateservice.feature.provider.dashboard.data.mapper.ProviderDashboardMapper
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats
import com.aggregateservice.feature.provider.dashboard.domain.repository.ProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Реализация ProviderRepository.
 *
 * Использует существующие booking endpoints как proxy до появления
 * dedicated provider dashboard endpoints.
 *
 * **Architecture:**
 * - Data layer реализует интерфейс Domain layer
 * - Использует ProviderDashboardApiService для сетевых запросов
 * - Использует ProviderDashboardMapper для преобразования DTO → Domain
 * - Earnings summary возвращает mock данные до появления backend endpoint
 *
 * @property apiService API сервис для provider dashboard
 * @property logger Логгер для observability (tag: "ProviderDashboard")
 */
class ProviderRepositoryImpl(
    private val apiService: ProviderDashboardApiService,
    private val logger: Logger,
) : ProviderRepository {
    override suspend fun getTodaysBookings(): Result<List<DashboardBooking>> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderRepositoryImpl") { "Fetching today's bookings" }

            // Try dedicated today's endpoint first
            val result = apiService.getTodaysBookings()

            result.fold(
                onSuccess = { dtos ->
                    val bookings = ProviderDashboardMapper.toDashboardBookingList(dtos)
                    logger.d("ProviderRepositoryImpl") {
                        "Fetched ${bookings.size} today's bookings"
                    }
                    Result.success(bookings)
                },
                onFailure = { error ->
                    logger.e("ProviderRepositoryImpl") {
                        "Failed to fetch today's bookings: ${error.message}"
                    }
                    Result.failure(error)
                },
            )
        }
    }

    override suspend fun getEarningsSummary(): Result<EarningsSummary> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderRepositoryImpl") {
                "getEarningsSummary() called - returning mock data"
            }

            // TODO: Backend endpoint /api/v1/providers/me/earnings does not exist yet.
            // Return mock data with 0.0 amounts until backend provides real endpoint.
            // When the endpoint is available, replace with:
            //   apiService.getEarningsSummary().map { dto -> toEarningsSummary(dto) }
            val mockSummary = EarningsSummary(
                todayAmount = 0.0,
                weekAmount = 0.0,
                monthAmount = 0.0,
                currency = "ILS",
            )

            logger.d("ProviderRepositoryImpl") {
                "Returning mock earnings summary: ${mockSummary.formattedToday}"
            }
            Result.success(mockSummary)
        }
    }

    override suspend fun getProviderStats(): Result<ProviderStats> {
        return withContext(Dispatchers.IO) {
            logger.d("ProviderRepositoryImpl") { "Fetching provider stats" }

            // Get today's bookings to compute stats
            val bookingsResult = apiService.getTodaysBookings()

            bookingsResult.fold(
                onSuccess = { dtos ->
                    val bookings = ProviderDashboardMapper.toDashboardBookingList(dtos)
                    val stats = ProviderDashboardMapper.computeStatsFromBookings(bookings)
                    logger.d("ProviderRepositoryImpl") {
                        "Computed stats from ${bookings.size} bookings: " +
                            "pending=${stats.pendingRequests}, " +
                            "active=${stats.activeBookings}, " +
                            "completed=${stats.completedToday}"
                    }
                    Result.success(stats)
                },
                onFailure = { error ->
                    logger.e("ProviderRepositoryImpl") {
                        "Failed to fetch provider stats: ${error.message}"
                    }
                    Result.failure(error)
                },
            )
        }
    }
}
