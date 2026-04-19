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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for CreateBookingUseCase.
 */
class CreateBookingUseCaseTest {
    private lateinit var createBookingUseCase: CreateBookingUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        createBookingUseCase = CreateBookingUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should create booking successfully`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1", "service-2")
            val startTime = getFutureTime(hoursAhead = 5)
            val notes = "Test booking"
            val expectedBooking = createTestBooking(providerId)

            mockRepository.createBookingResult = Result.success(expectedBooking)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime, notes)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(providerId, mockRepository.lastCreateProviderId)
            assertEquals(serviceIds, mockRepository.lastCreateServiceIds)
            assertEquals(startTime, mockRepository.lastCreateStartTime)
            assertEquals(notes, mockRepository.lastCreateNotes)
        }

    @Test
    fun `should create booking with null notes`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(hoursAhead = 3)

            mockRepository.createBookingResult = Result.success(createTestBooking(providerId))

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime, null)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(null, mockRepository.lastCreateNotes)
        }

    @Test
    fun `should create booking with single service`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(hoursAhead = 4)

            mockRepository.createBookingResult = Result.success(createTestBooking(providerId))

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(1, mockRepository.lastCreateServiceIds?.size)
        }

    @Test
    fun `should create booking with maximum allowed services`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = (1..10).map { "service-$it" }
            val startTime = getFutureTime(hoursAhead = 5)

            mockRepository.createBookingResult = Result.success(createTestBooking(providerId))

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(10, mockRepository.lastCreateServiceIds?.size)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when providerId is blank`() =
        runTest {
            // Arrange
            val providerId = "   "
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(hoursAhead = 5)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("providerId", error.field)
            assertEquals(ValidationRule.Required, error.rule)
        }

    @Test
    fun `should fail when serviceIds is empty`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = emptyList<String>()
            val startTime = getFutureTime(hoursAhead = 5)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("serviceIds", error.field)
            assertEquals(ValidationRule.NotEmpty, error.rule)
        }

    @Test
    fun `should fail when serviceIds exceeds maximum`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = (1..11).map { "service-$it" }
            val startTime = getFutureTime(hoursAhead = 5)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("serviceIds", error.field)
            assertEquals(ValidationRule.TooHigh, error.rule)
            assertEquals(10, error.parameters["max"])
        }

    @Test
    fun `should fail when startTime is too soon`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(hoursAhead = 1)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("startTime", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
            assertEquals(2L, error.parameters["min"])
        }

    @Test
    fun `should fail when startTime is too far in advance`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(daysAhead = 31)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("startTime", error.field)
            assertEquals(ValidationRule.TooHigh, error.rule)
            assertEquals(30L, error.parameters["max"])
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository create fails`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(hoursAhead = 5)
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.createBookingResult = Result.failure(expectedError)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository returns conflict`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val serviceIds = listOf("service-1")
            val startTime = getFutureTime(hoursAhead = 5)
            val expectedError = AppError.Conflict("Slot already booked")

            mockRepository.createBookingResult = Result.failure(expectedError)

            // Act
            val result = createBookingUseCase(providerId, serviceIds, startTime)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Conflict>(result.exceptionOrNull())
        }

    // ========== Helper Methods ==========

    private fun getFutureTime(hoursAhead: Int = 5, daysAhead: Int = 0): kotlinx.datetime.Instant {
        val now = Clock.System.now()
        val offsetMs = ((hoursAhead + daysAhead * 24) * 60L * 60L * 1000L)
        return kotlinx.datetime.Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + offsetMs)
    }

    private fun createTestBooking(providerId: String): Booking {
        val now = Clock.System.now()
        return Booking(
            id = "booking-123",
            providerId = providerId,
            providerName = "Test Salon",
            clientId = "client-456",
            startTime = getFutureTime(hoursAhead = 5),
            endTime = getFutureTime(hoursAhead = 6),
            status = BookingStatus.PENDING,
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
        var lastCreateProviderId: String? = null
        var lastCreateServiceIds: List<String>? = null
        var lastCreateStartTime: Instant? = null
        var lastCreateNotes: String? = null
        var lastGetBookingByIdBookingId: String? = null
        var lastGetClientBookingsStatus: String? = null
        var lastCancelBookingId: String? = null
        var lastCancelReason: String? = null
        var lastRescheduleBookingId: String? = null
        var lastRescheduleNewTime: Instant? = null
        var lastGetAvailableSlotsProviderId: String? = null
        var lastGetAvailableSlotsFromDate: LocalDate? = null
        var lastGetAvailableSlotsToDate: LocalDate? = null
        var lastGetAvailableSlotsServiceIds: List<String>? = null
        var lastGetProviderServicesProviderId: String? = null

        override suspend fun createBooking(
            providerId: String,
            serviceIds: List<String>,
            startTime: Instant,
            notes: String?,
        ): Result<Booking> {
            createBookingCallCount++
            lastCreateProviderId = providerId
            lastCreateServiceIds = serviceIds
            lastCreateStartTime = startTime
            lastCreateNotes = notes
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
            lastGetClientBookingsStatus = status
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
            lastGetAvailableSlotsProviderId = providerId
            lastGetAvailableSlotsFromDate = fromDate
            lastGetAvailableSlotsToDate = toDate
            lastGetAvailableSlotsServiceIds = serviceIds
            return getAvailableSlotsResult
        }

        override suspend fun getProviderServices(providerId: String): Result<List<com.aggregateservice.feature.booking.domain.model.BookingService>> {
            getProviderServicesCallCount++
            lastGetProviderServicesProviderId = providerId
            return getProviderServicesResult
        }
    }
}
