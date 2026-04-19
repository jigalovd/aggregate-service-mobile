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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for CancelBookingUseCase.
 */
class CancelBookingUseCaseTest {
    private lateinit var cancelBookingUseCase: CancelBookingUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        cancelBookingUseCase = CancelBookingUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should cancel booking successfully`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val reason = "Changed my mind"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val cancelledBooking = testBooking.copy(status = BookingStatus.CANCELLED)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.cancelBookingResult = Result.success(cancelledBooking)

            // Act
            val result = cancelBookingUseCase(bookingId, reason)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(bookingId, mockRepository.lastCancelBookingId)
            assertEquals(reason, mockRepository.lastCancelReason)
        }

    @Test
    fun `should cancel booking with null reason`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val cancelledBooking = testBooking.copy(status = BookingStatus.CANCELLED)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.cancelBookingResult = Result.success(cancelledBooking)

            // Act
            val result = cancelBookingUseCase(bookingId, null)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(null, mockRepository.lastCancelReason)
        }

    @Test
    fun `should cancel booking at minimum allowed time`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            // More than 2 hours before start to ensure success (edge case test)
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 3)
            val cancelledBooking = testBooking.copy(status = BookingStatus.CANCELLED)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.cancelBookingResult = Result.success(cancelledBooking)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `should cancel booking with reason containing special characters`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val reason = "Can't make it: doctor's appointment & meeting"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val cancelledBooking = testBooking.copy(status = BookingStatus.CANCELLED)

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.cancelBookingResult = Result.success(cancelledBooking)

            // Act
            val result = cancelBookingUseCase(bookingId, reason)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(reason, mockRepository.lastCancelReason)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when bookingId is blank`() =
        runTest {
            // Arrange
            val bookingId = "   "

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("bookingId", error.field)
            assertEquals(ValidationRule.Required, error.rule)
        }

    @Test
    fun `should fail when booking not found`() =
        runTest {
            // Arrange
            val bookingId = "nonexistent"
            mockRepository.getBookingByIdResult = Result.failure(AppError.NotFound)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    @Test
    fun `should fail when booking status is not cancellable`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5, status = BookingStatus.COMPLETED)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("status", error.field)
            assertEquals(ValidationRule.InvalidValue, error.rule)
        }

    @Test
    fun `should fail when cancelling too late`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            // Less than 2 hours before start - should fail
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 1)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("startTime", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
            assertEquals(2L, error.parameters["min"])
        }

    @Test
    fun `should fail for expired booking`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5, status = BookingStatus.EXPIRED)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("status", error.field)
        }

    @Test
    fun `should fail for already cancelled booking`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5, status = BookingStatus.CANCELLED)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("status", error.field)
        }

    @Test
    fun `should fail for pending booking in progress`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5, status = BookingStatus.IN_PROGRESS)
            mockRepository.getBookingByIdResult = Result.success(testBooking)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository cancel fails`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.cancelBookingResult = Result.failure(expectedError)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository returns conflict`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val testBooking = createTestBooking(bookingId, hoursUntilStart = 5)
            val expectedError = AppError.Conflict("Cannot cancel: already started")

            mockRepository.getBookingByIdResult = Result.success(testBooking)
            mockRepository.cancelBookingResult = Result.failure(expectedError)

            // Act
            val result = cancelBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Conflict>(result.exceptionOrNull())
        }

    // ========== Helper Methods ==========

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
        var lastCancelBookingId: String? = null
        var lastCancelReason: String? = null

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
            lastCancelBookingId = bookingId
            lastCancelReason = reason
            return cancelBookingResult
        }

        override suspend fun rescheduleBooking(bookingId: String, newStartTime: Instant): Result<Booking> {
            rescheduleBookingCallCount++
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