@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.provider.dashboard.presentation.screenmodel

import co.touchlab.kermit.Logger
import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.dashboard.domain.model.BookingStatus
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats
import com.aggregateservice.feature.provider.dashboard.domain.repository.ProviderRepository
import com.aggregateservice.feature.provider.dashboard.presentation.model.ProviderDashboardUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for ProviderDashboardScreenModel.
 * Uses functional mocks with behavior injection for predictable test scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProviderDashboardScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: MockProviderRepository
    private lateinit var logger: Logger

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MockProviderRepository()
        logger = Logger.withTag("ProviderDashboardTest")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel(): ProviderDashboardScreenModel {
        return ProviderDashboardScreenModel(
            repository = repository,
            logger = logger,
        )
    }

    @Test
    fun `initial load should emit Content on success`() = runTest {
        val expectedBookings = listOf(createTestBooking("booking-1"), createTestBooking("booking-2"))
        val expectedEarnings = EarningsSummary(todayAmount = 500.0, weekAmount = 2000.0, monthAmount = 8000.0)
        val expectedStats = ProviderStats(pendingRequests = 3, activeBookings = 5, completedToday = 2)

        repository.getTodaysBookingsResult = Result.success(expectedBookings)
        repository.getEarningsSummaryResult = Result.success(expectedEarnings)
        repository.getProviderStatsResult = Result.success(expectedStats)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Content>(state)
        assertEquals(2, state.todaysBookings.size)
        assertEquals(500.0, state.earningsSummary.todayAmount)
        assertEquals(3, state.providerStats.pendingRequests)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `initial load should emit Error on all API failures`() = runTest {
        repository.getTodaysBookingsResult = Result.failure(AppError.UnknownError(message = "Service Unavailable"))
        repository.getEarningsSummaryResult = Result.failure(AppError.UnknownError(message = "Service Unavailable"))
        repository.getProviderStatsResult = Result.failure(AppError.UnknownError(message = "Service Unavailable"))

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Error>(state)
    }

    @Test
    fun `initial load should emit Content with partial data on some failures`() = runTest {
        repository.getTodaysBookingsResult = Result.success(listOf(createTestBooking("booking-1")))
        repository.getEarningsSummaryResult = Result.failure(AppError.UnknownError(message = "Server Error"))
        repository.getProviderStatsResult = Result.success(ProviderStats(pendingRequests = 2, activeBookings = 1, completedToday = 0))

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Content>(state)
        assertEquals(1, state.todaysBookings.size)
    }

    @Test
    fun `empty bookings should show Content with empty list not Error`() = runTest {
        repository.getTodaysBookingsResult = Result.success(emptyList())
        repository.getEarningsSummaryResult = Result.success(EarningsSummary.empty())
        repository.getProviderStatsResult = Result.success(ProviderStats.empty())

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Content>(state)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `refresh should update existing state with new data`() = runTest {
        repository.getTodaysBookingsResult = Result.success(listOf(createTestBooking("old-booking")))
        repository.getEarningsSummaryResult = Result.success(EarningsSummary(todayAmount = 100.0, weekAmount = 0.0, monthAmount = 0.0))
        repository.getProviderStatsResult = Result.success(ProviderStats(pendingRequests = 1, activeBookings = 0, completedToday = 0))

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        repository.getTodaysBookingsResult = Result.success(listOf(createTestBooking("new-booking-1"), createTestBooking("new-booking-2")))
        repository.getEarningsSummaryResult = Result.success(EarningsSummary(todayAmount = 300.0, weekAmount = 0.0, monthAmount = 0.0))
        repository.getProviderStatsResult = Result.success(ProviderStats(pendingRequests = 3, activeBookings = 0, completedToday = 0))

        screenModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Content>(state)
        assertEquals(2, state.todaysBookings.size)
        assertEquals(300.0, state.earningsSummary.todayAmount)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `retry should reload data from error state`() = runTest {
        repository.getTodaysBookingsResult = Result.failure(AppError.UnknownError(message = "Error"))
        repository.getEarningsSummaryResult = Result.failure(AppError.UnknownError(message = "Error"))
        repository.getProviderStatsResult = Result.failure(AppError.UnknownError(message = "Error"))

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.uiState.value is ProviderDashboardUiState.Error)

        repository.getTodaysBookingsResult = Result.success(listOf(createTestBooking("retry-booking")))
        repository.getEarningsSummaryResult = Result.success(EarningsSummary(todayAmount = 150.0, weekAmount = 0.0, monthAmount = 0.0))
        repository.getProviderStatsResult = Result.success(ProviderStats(pendingRequests = 1, activeBookings = 0, completedToday = 0))

        screenModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Content>(state)
        assertEquals(1, state.todaysBookings.size)
    }

    @Test
    fun `should handle UnauthorizedError correctly`() = runTest {
        repository.getTodaysBookingsResult = Result.failure(AppError.Unauthorized)
        repository.getEarningsSummaryResult = Result.failure(AppError.Unauthorized)
        repository.getProviderStatsResult = Result.failure(AppError.Unauthorized)

        val screenModel = createScreenModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.uiState.value
        assertIs<ProviderDashboardUiState.Error>(state)
    }

    private fun createTestBooking(id: String): DashboardBooking {
        return DashboardBooking(
            id = id,
            clientName = "Test Client $id",
            startTime = Instant.fromEpochMilliseconds(0),
            endTime = Instant.fromEpochMilliseconds(3600000),
            serviceName = "Test Service",
            status = BookingStatus.CONFIRMED,
            totalPrice = 100.0,
        )
    }
}

/**
 * Mock implementation of ProviderRepository for testing.
 */
private class MockProviderRepository : ProviderRepository {
    var getTodaysBookingsResult: Result<List<DashboardBooking>> = Result.success(emptyList())
    var getEarningsSummaryResult: Result<EarningsSummary> = Result.success(EarningsSummary.empty())
    var getProviderStatsResult: Result<ProviderStats> = Result.success(ProviderStats.empty())

    var getTodaysBookingsCallCount = 0
    var getEarningsSummaryCallCount = 0
    var getProviderStatsCallCount = 0

    override suspend fun getTodaysBookings(): Result<List<DashboardBooking>> {
        getTodaysBookingsCallCount++
        return getTodaysBookingsResult
    }

    override suspend fun getEarningsSummary(): Result<EarningsSummary> {
        getEarningsSummaryCallCount++
        return getEarningsSummaryResult
    }

    override suspend fun getProviderStats(): Result<ProviderStats> {
        getProviderStatsCallCount++
        return getProviderStatsResult
    }
}