package com.aggregateservice.feature.provider.dashboard.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for DashboardBooking domain model.
 */
class DashboardBookingTest {

    @Test
    fun `formattedPrice returns correct format`() {
        val booking = createBooking(totalPrice = 150.0)
        assertEquals("150 ₪", booking.formattedPrice)
    }

    @Test
    fun `formattedPrice handles zero price`() {
        val booking = createBooking(totalPrice = 0.0)
        assertEquals("0 ₪", booking.formattedPrice)
    }

    @Test
    fun `durationMinutes calculates correct duration`() {
        val startTime = Instant.fromEpochMilliseconds(0L)
        val endTime = Instant.fromEpochMilliseconds(3600000L) // 60 min in ms
        val booking = DashboardBooking(
            id = "test-id",
            clientName = "Test Client",
            startTime = startTime,
            endTime = endTime,
            serviceName = "Haircut",
            status = BookingStatus.CONFIRMED,
            totalPrice = 100.0,
        )
        assertEquals(60, booking.durationMinutes)
    }

    @Test
    fun `formattedDuration returns correct format`() {
        val startTime = Instant.fromEpochMilliseconds(0L)
        val endTime = Instant.fromEpochMilliseconds(3600000L) // 60 min
        val booking = DashboardBooking(
            id = "test-id",
            clientName = "Test Client",
            startTime = startTime,
            endTime = endTime,
            serviceName = "Haircut",
            status = BookingStatus.CONFIRMED,
            totalPrice = 100.0,
        )
        assertEquals("60 min", booking.formattedDuration)
    }

    @Test
    fun `isActive returns true for PENDING status`() {
        val booking = createBooking(status = BookingStatus.PENDING)
        assertTrue(booking.isActive)
    }

    @Test
    fun `isActive returns true for CONFIRMED status`() {
        val booking = createBooking(status = BookingStatus.CONFIRMED)
        assertTrue(booking.isActive)
    }

    @Test
    fun `isActive returns true for IN_PROGRESS status`() {
        val booking = createBooking(status = BookingStatus.IN_PROGRESS)
        assertTrue(booking.isActive)
    }

    @Test
    fun `isActive returns false for COMPLETED status`() {
        val booking = createBooking(status = BookingStatus.COMPLETED)
        assertFalse(booking.isActive)
    }

    @Test
    fun `isActive returns false for CANCELLED status`() {
        val booking = createBooking(status = BookingStatus.CANCELLED)
        assertFalse(booking.isActive)
    }

    @Test
    fun `empty creates booking with default values`() {
        val empty = DashboardBooking.empty()
        assertEquals("", empty.id)
        assertEquals("", empty.clientName)
        assertEquals("", empty.serviceName)
        assertEquals(BookingStatus.PENDING, empty.status)
        assertEquals(0.0, empty.totalPrice)
    }

    private fun createBooking(
        id: String = "test-id",
        clientName: String = "Test Client",
        startTime: Instant = Instant.fromEpochMilliseconds(1000),
        endTime: Instant = Instant.fromEpochMilliseconds(3700001), // 60 min 1 sec
        serviceName: String = "Haircut",
        status: BookingStatus = BookingStatus.CONFIRMED,
        totalPrice: Double = 100.0,
    ) = DashboardBooking(
        id = id,
        clientName = clientName,
        startTime = startTime,
        endTime = endTime,
        serviceName = serviceName,
        status = status,
        totalPrice = totalPrice,
    )
}
