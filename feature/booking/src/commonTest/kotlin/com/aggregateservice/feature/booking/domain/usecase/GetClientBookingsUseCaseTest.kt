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
import kotlinx.datetime.todayIn
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for GetClientBookingsUseCase.
 */
class GetClientBookingsUseCaseTest {
    private lateinit var getClientBookingsUseCase: GetClientBookingsUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        getClientBookingsUseCase = GetClientBookingsUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return bookings on successful fetch`() =
        runTest {
            // Arrange
            val expectedBookings = listOf(createTestBooking("booking-1"), createTestBooking("booking-2"))
            mockRepository.getClientBookingsResult = Result.success(expectedBookings)

            // Act
            val result = getClientBookingsUseCase()

            // Assert
            assertTrue(result.isSuccess)
            val bookings = result.getOrNull()!!
            assertEquals(2, bookings.size)
        }

    @Test
    fun `should return empty list when no bookings exist`() =
        runTest {
            // Arrange
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            val result = getClientBookingsUseCase()

            // Assert
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.isEmpty())
        }

    @Test
    fun `should pass status filter to repository`() =
        runTest {
            // Arrange
            val status = "CONFIRMED"
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            getClientBookingsUseCase(status = status)

            // Assert
            assertEquals(status, mockRepository.lastGetClientBookingsStatus)
        }

    @Test
    fun `should pass null status for all bookings`() =
        runTest {
            // Arrange
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            getClientBookingsUseCase(status = null)

            // Assert
            assertEquals(null, mockRepository.lastGetClientBookingsStatus)
        }

    @Test
    fun `should use default pagination values`() =
        runTest {
            // Arrange
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            getClientBookingsUseCase()

            // Assert
            assertEquals(1, mockRepository.getClientBookingsCallCount)
        }

    @Test
    fun `should allow custom page size`() =
        runTest {
            // Arrange
            val pageSize = 50
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            getClientBookingsUseCase(page = 1, pageSize = pageSize)

            // Assert
            assertEquals(pageSize, mockRepository.lastGetClientBookingsPageSize)
        }

    @Test
    fun `should allow first page`() =
        runTest {
            // Arrange
            val page = 1
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            getClientBookingsUseCase(page = page)

            // Assert
            assertEquals(page, mockRepository.lastGetClientBookingsPage)
        }

    @Test
    fun `should allow last page`() =
        runTest {
            // Arrange
            val page = 10
            mockRepository.getClientBookingsResult = Result.success(emptyList())

            // Act
            getClientBookingsUseCase(page = page)

            // Assert
            assertEquals(page, mockRepository.lastGetClientBookingsPage)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when page is less than 1`() =
        runTest {
            // Arrange
            val page = 0

            // Act
            val result = getClientBookingsUseCase(page = page)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("page", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
        }

    @Test
    fun `should fail when page is negative`() =
        runTest {
            // Arrange
            val page = -1

            // Act
            val result = getClientBookingsUseCase(page = page)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("page", error.field)
        }

    @Test
    fun `should fail when pageSize is less than 1`() =
        runTest {
            // Arrange
            val pageSize = 0

            // Act
            val result = getClientBookingsUseCase(pageSize = pageSize)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("pageSize", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
        }

    @Test
    fun `should fail when pageSize exceeds maximum`() =
        runTest {
            // Arrange
            val pageSize = 101

            // Act
            val result = getClientBookingsUseCase(pageSize = pageSize)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("pageSize", error.field)
            assertEquals(ValidationRule.TooHigh, error.rule)
            assertEquals(100, error.parameters["max"])
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository fails`() =
        runTest {
            // Arrange
            val expectedError = AppError.Unauthorized
            mockRepository.getClientBookingsResult = Result.failure(expectedError)

            // Act
            val result = getClientBookingsUseCase()

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return network error on service unavailable`() =
        runTest {
            // Arrange
            val expectedError = AppError.NetworkError(503, "Service Unavailable")
            mockRepository.getClientBookingsResult = Result.failure(expectedError)

            // Act
            val result = getClientBookingsUseCase()

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NetworkError>(result.exceptionOrNull())
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
        var lastGetClientBookingsStatus: String? = null
        var lastGetClientBookingsPage: Int = 1
        var lastGetClientBookingsPageSize: Int = 20

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
            return getBookingByIdResult
        }

        override suspend fun getClientBookings(
            status: String?,
            page: Int,
            pageSize: Int,
        ): Result<List<Booking>> {
            getClientBookingsCallCount++
            lastGetClientBookingsStatus = status
            lastGetClientBookingsPage = page
            lastGetClientBookingsPageSize = pageSize
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
