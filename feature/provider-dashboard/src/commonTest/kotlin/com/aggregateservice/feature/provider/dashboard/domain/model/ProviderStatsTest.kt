package com.aggregateservice.feature.provider.dashboard.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for ProviderStats domain model.
 */
class ProviderStatsTest {

    @Test
    fun `totalBookings calculates sum correctly`() {
        val stats = ProviderStats(
            pendingRequests = 3,
            activeBookings = 5,
            completedToday = 2,
        )
        assertEquals(10, stats.totalBookings)
    }

    @Test
    fun `totalBookings handles zero values`() {
        val stats = ProviderStats(
            pendingRequests = 0,
            activeBookings = 0,
            completedToday = 0,
        )
        assertEquals(0, stats.totalBookings)
    }

    @Test
    fun `empty returns zero values`() {
        val empty = ProviderStats.empty()
        assertEquals(0, empty.pendingRequests)
        assertEquals(0, empty.activeBookings)
        assertEquals(0, empty.completedToday)
        assertEquals(0, empty.totalBookings)
    }

    @Test
    fun `totalBookings with only pending requests`() {
        val stats = ProviderStats(
            pendingRequests = 5,
            activeBookings = 0,
            completedToday = 0,
        )
        assertEquals(5, stats.totalBookings)
    }

    @Test
    fun `totalBookings with only active bookings`() {
        val stats = ProviderStats(
            pendingRequests = 0,
            activeBookings = 10,
            completedToday = 0,
        )
        assertEquals(10, stats.totalBookings)
    }

    @Test
    fun `totalBookings with only completed today`() {
        val stats = ProviderStats(
            pendingRequests = 0,
            activeBookings = 0,
            completedToday = 8,
        )
        assertEquals(8, stats.totalBookings)
    }
}
