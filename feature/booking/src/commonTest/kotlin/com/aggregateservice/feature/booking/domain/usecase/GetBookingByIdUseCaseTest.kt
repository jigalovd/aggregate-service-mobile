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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for GetBookingByIdUseCase.
 */
class GetBookingByIdUseCaseTest {
    private lateinit var getBookingByIdUseCase: GetBookingByIdUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        getBookingByIdUseCase = GetBookingByIdUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return booking on successful fetch`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val expectedBooking = createTestBooking(bookingId)
            mockRepository.getBookingByIdResult = Result.success(expectedBooking)

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(bookingId, result.getOrNull()!!.id)
        }

    @Test
    fun `should call repository with correct bookingId`() =
        runTest {
            // Arrange
            val bookingId = "booking-456"
            mockRepository.getBookingByIdResult = Result.success(createTestBooking(bookingId))

            // Act
            getBookingByIdUseCase(bookingId)

            // Assert
            assertEquals(1, mockRepository.getBookingByIdCallCount)
            assertEquals(bookingId, mockRepository.lastGetBookingByIdBookingId)
        }

    @Test
    fun `should return booking with all properties`() =
        runTest {
            // Arrange
            val bookingId = "booking-789"
            val expectedBooking = createTestBooking(bookingId)
            mockRepository.getBookingByIdResult = Result.success(expectedBooking)

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isSuccess)
            val booking = result.getOrNull()!!
            assertEquals("booking-789", booking.id)
            assertEquals("provider-123", booking.providerId)
            assertEquals("Test Salon", booking.providerName)
            assertEquals("client-456", booking.clientId)
            assertEquals(BookingStatus.CONFIRMED, booking.status)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when bookingId is blank`() =
        runTest {
            // Arrange
            val bookingId = "   "

            // Act
            val result = getBookingByIdUseCase(bookingId)

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

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("bookingId", error.field)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when booking not found`() =
        runTest {
            // Arrange
            val bookingId = "nonexistent"
            val expectedError = AppError.NotFound
            mockRepository.getBookingByIdResult = Result.failure(expectedError)

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository fails with network error`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val expectedError = AppError.NetworkError(503, "Service Unavailable")
            mockRepository.getBookingByIdResult = Result.failure(expectedError)

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NetworkError>(result.exceptionOrNull())
            assertEquals(503, (result.exceptionOrNull() as AppError.NetworkError).code)
        }

    @Test
    fun `should return error when repository fails with unauthorized`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val expectedError = AppError.Unauthorized
            mockRepository.getBookingByIdResult = Result.failure(expectedError)

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Unauthorized>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository throws unexpected exception`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            mockRepository.getBookingByIdResult = Result.failure(RuntimeException("Unexpected"))

            // Act
            val result = getBookingByIdUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() != null)
        }

    // ========== Helper Methods ==========

    private fun createTestBooking(id: String): Booking {
        val now = Clock.System.now()
        return Booking(
            id = id,
            providerId = "provider-123",
            providerName = "Test Salon",
            clientId = "client-456",
            startTime = now.plusHours(5),
            endTime = now.plusHours(6),
            status = BookingStatus.CONFIRMED,
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
