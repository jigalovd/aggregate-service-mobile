package com.aggregateservice.feature.provider.dashboard.presentation.model

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for ProviderDashboardUiState sealed class.
 */
class ProviderDashboardUiStateTest {

    @Test
    fun `Loading is singleton Loading state`() {
        val loading = ProviderDashboardUiState.Loading
        assertIs<ProviderDashboardUiState.Loading>(loading)
    }

    @Test
    fun `Content has correct default values`() {
        val content = ProviderDashboardUiState.Content()
        assertTrue(content.todaysBookings.isEmpty())
        assertEquals(EarningsSummary.empty(), content.earningsSummary)
        assertEquals(ProviderStats.empty(), content.providerStats)
        assertFalse(content.isRefreshing)
        assertTrue(content.isEmpty)
    }

    @Test
    fun `Content isEmpty returns false when bookings exist`() {
        val booking = DashboardBooking(
            id = "test",
            clientName = "Test",
            startTime = Instant.fromEpochMilliseconds(0),
            endTime = Instant.fromEpochMilliseconds(0),
            serviceName = "Service",
            status = com.aggregateservice.feature.provider.dashboard.domain.model.BookingStatus.CONFIRMED,
            totalPrice = 100.0,
        )
        val content = ProviderDashboardUiState.Content(todaysBookings = listOf(booking))
        assertFalse(content.isEmpty)
    }

    @Test
    fun `content helper creates Content with correct values`() {
        val bookings = listOf(DashboardBooking.empty())
        val earnings = EarningsSummary(todayAmount = 100.0, weekAmount = 500.0, monthAmount = 2000.0)
        val stats = ProviderStats(pendingRequests = 3, activeBookings = 5, completedToday = 2)

        val content = ProviderDashboardUiState.content(
            todaysBookings = bookings,
            earningsSummary = earnings,
            providerStats = stats,
        )

        assertEquals(bookings, content.todaysBookings)
        assertEquals(earnings, content.earningsSummary)
        assertEquals(stats, content.providerStats)
    }

    @Test
    fun `Error has correct default values`() {
        val error = AppError.NetworkError(500, "Server error")
        val errorState = ProviderDashboardUiState.Error(error)

        assertEquals(error, errorState.error)
        assertTrue(errorState.todaysBookings.isEmpty())
        assertEquals(EarningsSummary.empty(), errorState.earningsSummary)
        assertEquals(ProviderStats.empty(), errorState.providerStats)
        assertFalse(errorState.hasCachedData)
    }

    @Test
    fun `Error hasCachedData returns true when bookings exist`() {
        val error = AppError.NetworkError(500, "Server error")
        val booking = DashboardBooking.empty()
        val errorState = ProviderDashboardUiState.Error(
            error = error,
            todaysBookings = listOf(booking),
        )
        assertTrue(errorState.hasCachedData)
    }

    @Test
    fun `Error hasCachedData returns true when earnings exist`() {
        val error = AppError.NetworkError(500, "Server error")
        val earnings = EarningsSummary(todayAmount = 100.0, weekAmount = 0.0, monthAmount = 0.0)
        val errorState = ProviderDashboardUiState.Error(
            error = error,
            earningsSummary = earnings,
        )
        assertTrue(errorState.hasCachedData)
    }

    @Test
    fun `Error hasCachedData returns true when stats exist`() {
        val error = AppError.NetworkError(500, "Server error")
        val stats = ProviderStats(pendingRequests = 0, activeBookings = 5, completedToday = 0)
        val errorState = ProviderDashboardUiState.Error(
            error = error,
            providerStats = stats,
        )
        assertTrue(errorState.hasCachedData)
    }

    @Test
    fun `error helper creates Error with correct values`() {
        val error = AppError.NotFound
        val bookings = listOf(DashboardBooking.empty())
        val earnings = EarningsSummary.empty()
        val stats = ProviderStats.empty()

        val errorState = ProviderDashboardUiState.error(
            error = error,
            todaysBookings = bookings,
            earningsSummary = earnings,
            providerStats = stats,
        )

        assertEquals(error, errorState.error)
        assertEquals(bookings, errorState.todaysBookings)
        assertEquals(earnings, errorState.earningsSummary)
        assertEquals(stats, errorState.providerStats)
    }
}
