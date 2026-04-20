package com.aggregateservice.feature.provider.dashboard.data.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.dashboard.domain.model.BookingStatus
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats
import com.aggregateservice.feature.provider.dashboard.domain.repository.ProviderRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for ProviderRepository behavior using a testable implementation.
 */
class ProviderRepositoryImplTest {
    private lateinit var repository: TestableProviderRepository

    @BeforeTest
    fun setup() {
        repository = TestableProviderRepository()
    }

    @Test
    fun `getTodaysBookings should return bookings on success`() = runTest {
        val bookings = listOf(createTestBooking("booking-1"), createTestBooking("booking-2"))
        repository.setTodaysBookingsResult(Result.success(bookings))

        val result = repository.getTodaysBookings()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `getTodaysBookings should return empty list on success with empty response`() = runTest {
        repository.setTodaysBookingsResult(Result.success(emptyList()))

        val result = repository.getTodaysBookings()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getTodaysBookings should return error on API failure`() = runTest {
        repository.setTodaysBookingsResult(Result.failure(AppError.UnknownError(message = "Network error")))

        val result = repository.getTodaysBookings()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError)
    }

    @Test
    fun `getEarningsSummary should return mock data with zero values`() = runTest {
        val result = repository.getEarningsSummary()

        assertTrue(result.isSuccess)
        val summary = result.getOrNull()!!
        assertEquals(0.0, summary.todayAmount)
        assertEquals("ILS", summary.currency)
    }

    @Test
    fun `getEarningsSummary should return custom mock data when configured`() = runTest {
        repository.setEarningsSummaryMock(EarningsSummary(todayAmount = 500.0, weekAmount = 2500.0, monthAmount = 10000.0))

        val result = repository.getEarningsSummary()

        assertTrue(result.isSuccess)
        assertEquals(500.0, result.getOrNull()!!.todayAmount)
    }

    @Test
    fun `getProviderStats should compute stats from bookings`() = runTest {
        val bookings = listOf(
            createTestBookingWithStatus("pending-1", BookingStatus.PENDING),
            createTestBookingWithStatus("confirmed-1", BookingStatus.CONFIRMED),
            createTestBookingWithStatus("confirmed-2", BookingStatus.CONFIRMED),
            createTestBookingWithStatus("completed-1", BookingStatus.COMPLETED),
        )
        repository.setTodaysBookingsResult(Result.success(bookings))

        val result = repository.getProviderStats()

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(1, stats.pendingRequests)
        assertEquals(2, stats.activeBookings)
        assertEquals(1, stats.completedToday)
    }

    @Test
    fun `getProviderStats should return empty stats when no bookings`() = runTest {
        repository.setTodaysBookingsResult(Result.success(emptyList()))

        val result = repository.getProviderStats()

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(0, stats.pendingRequests)
    }

    @Test
    fun `getProviderStats should return error when API fails`() = runTest {
        repository.setTodaysBookingsResult(Result.failure(AppError.UnknownError(message = "Server Error")))

        val result = repository.getProviderStats()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError)
    }

    private fun createTestBooking(id: String): DashboardBooking {
        return DashboardBooking(
            id = id,
            clientName = "Test Client",
            startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(0),
            endTime = kotlinx.datetime.Instant.fromEpochMilliseconds(3600000),
            serviceName = "Test Service",
            status = BookingStatus.CONFIRMED,
            totalPrice = 100.0,
        )
    }

    private fun createTestBookingWithStatus(id: String, status: BookingStatus): DashboardBooking {
        return DashboardBooking(
            id = id,
            clientName = "Test Client",
            startTime = kotlinx.datetime.Instant.fromEpochMilliseconds(0),
            endTime = kotlinx.datetime.Instant.fromEpochMilliseconds(3600000),
            serviceName = "Test Service",
            status = status,
            totalPrice = 100.0,
        )
    }
}

/**
 * Testable implementation that follows the repository contract.
 */
private class TestableProviderRepository : ProviderRepository {
    private var todaysBookingsResult: Result<List<DashboardBooking>> = Result.success(emptyList())
    private var earningsSummaryMock: EarningsSummary? = null

    fun setTodaysBookingsResult(result: Result<List<DashboardBooking>>) {
        todaysBookingsResult = result
    }

    fun setEarningsSummaryMock(summary: EarningsSummary) {
        earningsSummaryMock = summary
    }

    override suspend fun getTodaysBookings(): Result<List<DashboardBooking>> = todaysBookingsResult

    override suspend fun getEarningsSummary(): Result<EarningsSummary> {
        return Result.success(earningsSummaryMock ?: EarningsSummary.empty())
    }

    override suspend fun getProviderStats(): Result<ProviderStats> {
        return todaysBookingsResult.map { bookings ->
            ProviderStats(
                pendingRequests = bookings.count { it.status == BookingStatus.PENDING },
                activeBookings = bookings.count {
                    it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.IN_PROGRESS
                },
                completedToday = bookings.count { it.status == BookingStatus.COMPLETED },
            )
        }
    }
}