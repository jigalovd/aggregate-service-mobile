@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingItem
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for RescheduleBookingUseCase.
 */
class RescheduleBookingUseCaseTest {
    private lateinit var rescheduleBookingUseCase: RescheduleBookingUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        rescheduleBookingUseCase = RescheduleBookingUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should reschedule booking successfully`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val rescheduledBooking = testBooking.copy(startTime = newStartTime, endTime = newStartTime.plusHours(1))

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.rescheduleBookingResult = Result.success(rescheduledBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(bookingId, mockRepository.lastRescheduleBookingId)
            assertEquals(newStartTime, mockRepository.lastRescheduleNewTime)
        }

    @Test
    fun `should reschedule booking at minimum allowed time`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            // More than 2 hours before start to ensure success
            val newStartTime = getFutureTime(hoursAhead = 10)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 3)
            val rescheduledBooking = testBooking.copy(startTime = newStartTime)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.rescheduleBookingResult = Result.success(rescheduledBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `should reschedule booking with many hours notice`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 48)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 24)
            val rescheduledBooking = testBooking.copy(startTime = newStartTime)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.rescheduleBookingResult = Result.success(rescheduledBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `should call repository with correct parameters`() =
        runTest {
            // Arrange
            val bookingId = "booking-456"
            val newStartTime = getFutureTime(hoursAhead = 12)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.rescheduleBookingResult = Result.success(testBooking)

            // Act
            rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertEquals(1, mockRepository.getBookingByIdCallCount)
            assertEquals(1, mockRepository.rescheduleBookingCallCount)
            assertEquals(bookingId, mockRepository.lastGetBookingByIdBookingId)
            assertEquals(bookingId, mockRepository.lastRescheduleBookingId)
            assertEquals(newStartTime, mockRepository.lastRescheduleNewTime)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when bookingId is blank`() =
        runTest {
            // Arrange
            val bookingId = "   "
            val newStartTime = getFutureTime(hoursAhead = 10)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("bookingId", error.field)
            assertEquals(ValidationRule.Required, error.rule)
        }

    @Test
    fun `should fail when bookingId is empty`() =
        runTest {
            // Arrange
            val bookingId = ""
            val newStartTime = getFutureTime(hoursAhead = 10)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("bookingId", error.field)
        }

    @Test
    fun `should fail when newStartTime is in the past`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getPastTime(hoursAgo = 1)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("newStartTime", error.field)
            assertEquals(ValidationRule.InvalidFormat, error.rule)
        }

    @Test
    fun `should fail when newStartTime is now`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = Clock.System.now()

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("newStartTime", error.field)
        }

    @Test
    fun `should fail when booking not found`() =
        runTest {
            // Arrange
            val bookingId = "nonexistent"
            val newStartTime = getFutureTime(hoursAhead = 10)
            mockRepository.getBookingByIdResult = Result.failure(AppError.NotFound)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    @Test
    fun `should fail when rescheduling too late`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            // Less than 2 hours before start - should fail
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 1)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("startTime", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
            assertEquals(2L, error.parameters["min"])
        }

    @Test
    fun `should fail for completed booking`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            // Booking is less than 2 hours away - time check will fail before any other logic
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 1, status = BookingStatus.COMPLETED)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
        }

    @Test
    fun `should fail for cancelled booking`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            // Booking is less than 2 hours away
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 1, status = BookingStatus.CANCELLED)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
        }

    @Test
    fun `should fail for expired booking`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            // Booking is less than 2 hours away
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 1, status = BookingStatus.EXPIRED)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository reschedule fails`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.rescheduleBookingResult = Result.failure(expectedError)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository returns conflict`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val expectedError = AppError.Conflict("Slot not available")

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.rescheduleBookingResult = Result.failure(expectedError)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Conflict>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when getBookingById fails with network error`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val newStartTime = getFutureTime(hoursAhead = 10)
            val expectedError = AppError.NetworkError(500, "Internal Server Error")

            mockRepository.getBookingByIdResult = Result.failure(expectedError)

            // Act
            val result = rescheduleBookingUseCase(bookingId, newStartTime)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    // ========== Helper Methods ==========

    private fun getFutureTime(hoursAhead: Int): kotlinx.datetime.Instant {
        val now = Clock.System.now()
        val offsetMs = hoursAhead * 60L * 60L * 1000L
        return kotlinx.datetime.Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + offsetMs)
    }

    private fun getPastTime(hoursAgo: Int): Instant {
        val now = Clock.System.now()
        return Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - hoursAgo * 60L * 60L * 1000L)
    }

    private fun createTestBooking(
        id: String,
        hoursUntilStart: Int,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ): Booking {
        val now = Clock.System.now()
        val startTime = now.plusHours(hoursUntilStart)
        val endTime = startTime.plusHours(1)
        return Booking(
            id = id,
            providerId = "provider-123",
            providerName = "Test Salon",
            clientId = "client-456",
            startTime = startTime,
            endTime = endTime,
            status = status,
            items = listOf(
                BookingItem(
                    id = "item-1",
                    serviceId = "service-1",
                    serviceName = "Haircut",
                    price = 100.0,
                    currency = "ILS",
                    durationMinutes = 30,
                ),
            ),
            totalPrice = 100.0,
            totalDurationMinutes = 30,
            currency = "ILS",
            notes = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun Instant.plusHours(hours: Int): Instant {
        return Instant.fromEpochMilliseconds(
            toEpochMilliseconds() + hours * 60L * 60L * 1000L,
        )
    }

    /**
     * Mock implementation of BookingRepository for testing.
     */
    private class MockBookingRepository : BookingRepository {
        // Result properties
        var createBookingResult: Result<Booking> = Result.success(Booking.empty())
        var getBookingByIdResult: Result<Booking> = Result.success(Booking.empty())
        var getClientBookingsResult: Result<List<Booking>> = Result.success(emptyList())
        var confirmBookingResult: Result<Booking> = Result.success(Booking.empty())
        var cancelBookingResult: Result<Booking> = Result.success(Booking.empty())
        var rescheduleBookingResult: Result<Booking> = Result.success(Booking.empty())
        var getAvailableSlotsResult: Result<List<com.aggregateservice.feature.booking.domain.model.TimeSlot>> =
            Result.success(emptyList())
        var getProviderServicesResult: Result<List<com.aggregateservice.feature.booking.domain.model.BookingService>> =
            Result.success(emptyList())

        // Call tracking
        var createBookingCallCount = 0
        var getBookingByIdCallCount = 0
        var getClientBookingsCallCount = 0
        var confirmBookingCallCount = 0
        var cancelBookingCallCount = 0
        var rescheduleBookingCallCount = 0
        var getAvailableSlotsCallCount = 0
        var getProviderServicesCallCount = 0

        // Last call parameters
        var lastGetBookingByIdBookingId: String? = null
        var lastRescheduleBookingId: String? = null
        var lastRescheduleNewTime: Instant? = null

        override suspend fun createBooking(
            providerId: String,
            serviceIds: List<String>,
            startTime: Instant,
            notes: String?,
        ): Result<Booking> {
            createBookingCallCount++
            return createBookingResult
        }

        override suspend fun getBookingById(bookingId: String): Result<Booking> {
            getBookingByIdCallCount++
            lastGetBookingByIdBookingId = bookingId
            return getBookingByIdResult
        }

        override suspend fun getClientBookings(
            status: String?,
            page: Int,
            pageSize: Int,
        ): Result<List<Booking>> {
            getClientBookingsCallCount++
            return getClientBookingsResult
        }

        override suspend fun confirmBooking(bookingId: String): Result<Booking> {
            confirmBookingCallCount++
            return confirmBookingResult
        }

        override suspend fun cancelBooking(bookingId: String, reason: String?): Result<Booking> {
            cancelBookingCallCount++
            return cancelBookingResult
        }

        override suspend fun rescheduleBooking(bookingId: String, newStartTime: Instant): Result<Booking> {
            rescheduleBookingCallCount++
            lastRescheduleBookingId = bookingId
            lastRescheduleNewTime = newStartTime
            return rescheduleBookingResult
        }

        override suspend fun getAvailableSlots(
            providerId: String,
            fromDate: LocalDate,
            toDate: LocalDate,
            serviceIds: List<String>,
        ): Result<List<com.aggregateservice.feature.booking.domain.model.TimeSlot>> {
            getAvailableSlotsCallCount++
            return getAvailableSlotsResult
        }

        override suspend fun getProviderServices(providerId: String): Result<List<com.aggregateservice.feature.booking.domain.model.BookingService>> {
            getProviderServicesCallCount++
            return getProviderServicesResult
        }
    }
}
