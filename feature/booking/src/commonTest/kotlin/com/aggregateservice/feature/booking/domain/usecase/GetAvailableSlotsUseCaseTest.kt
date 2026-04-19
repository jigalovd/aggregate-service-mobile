@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.TimeSlot
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
 * Tests for GetAvailableSlotsUseCase.
 */
class GetAvailableSlotsUseCaseTest {
    private lateinit var getAvailableSlotsUseCase: GetAvailableSlotsUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        getAvailableSlotsUseCase = GetAvailableSlotsUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return available slots on successful fetch`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = listOf("service-1")
            val expectedSlots = createTestSlots(providerId, 3)

            mockRepository.getAvailableSlotsResult = Result.success(expectedSlots)

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isSuccess)
            val slots = result.getOrNull()!!
            assertEquals(3, slots.size)
        }

    @Test
    fun `should return empty list when no slots available`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = listOf("service-1")

            mockRepository.getAvailableSlotsResult = Result.success(emptyList())

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.isEmpty())
        }

    @Test
    fun `should pass correct parameters to repository`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(14)
            val serviceIds = listOf("service-1", "service-2")

            mockRepository.getAvailableSlotsResult = Result.success(emptyList())

            // Act
            getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertEquals(1, mockRepository.getAvailableSlotsCallCount)
            assertEquals(providerId, mockRepository.lastGetAvailableSlotsProviderId)
            assertEquals(fromDate, mockRepository.lastGetAvailableSlotsFromDate)
            assertEquals(toDate, mockRepository.lastGetAvailableSlotsToDate)
            assertEquals(serviceIds, mockRepository.lastGetAvailableSlotsServiceIds)
        }

    @Test
    fun `should accept single day date range`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val date = getToday()
            val serviceIds = listOf("service-1")

            mockRepository.getAvailableSlotsResult = Result.success(emptyList())

            // Act
            val result = getAvailableSlotsUseCase(providerId, date, date, serviceIds)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(date, mockRepository.lastGetAvailableSlotsFromDate)
            assertEquals(date, mockRepository.lastGetAvailableSlotsToDate)
        }

    @Test
    fun `should accept week-long date range`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = listOf("service-1")

            mockRepository.getAvailableSlotsResult = Result.success(emptyList())

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `should return available slots with correct properties`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(1)
            val serviceIds = listOf("service-1")
            val now = Clock.System.now()
            val slot1Start = now.plusHours(3)
            val slot1End = now.plusHours(4)
            val expectedSlots = listOf(
                TimeSlot(
                    startTime = slot1Start,
                    endTime = slot1End,
                    isAvailable = true,
                    providerId = providerId,
                ),
            )

            mockRepository.getAvailableSlotsResult = Result.success(expectedSlots)

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isSuccess)
            val slot = result.getOrNull()!!.first()
            assertTrue(slot.isAvailable)
            assertEquals(providerId, slot.providerId)
            assertEquals(slot1Start, slot.startTime)
            assertEquals(slot1End, slot.endTime)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when providerId is blank`() =
        runTest {
            // Arrange
            val providerId = "   "
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = listOf("service-1")

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

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
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = emptyList<String>()

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("serviceIds", error.field)
            assertEquals(ValidationRule.NotEmpty, error.rule)
        }

    @Test
    fun `should fail when fromDate is in the past`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday().minusDays(1)
            val toDate = getToday().plusDays(7)
            val serviceIds = listOf("service-1")

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("fromDate", error.field)
            assertEquals(ValidationRule.InvalidFormat, error.rule)
        }

    @Test
    fun `should fail when toDate is before fromDate`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday().plusDays(7)
            val toDate = getToday()
            val serviceIds = listOf("service-1")

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("toDate", error.field)
            assertEquals(ValidationRule.InvalidFormat, error.rule)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository fails`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = listOf("service-1")
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.getAvailableSlotsResult = Result.failure(expectedError)

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository returns not found`() =
        runTest {
            // Arrange
            val providerId = "nonexistent"
            val fromDate = getToday()
            val toDate = fromDate.plusDays(7)
            val serviceIds = listOf("service-1")

            mockRepository.getAvailableSlotsResult = Result.failure(AppError.NotFound)

            // Act
            val result = getAvailableSlotsUseCase(providerId, fromDate, toDate, serviceIds)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    // ========== Helper Methods ==========

    private fun getToday(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    private fun createTestSlots(providerId: String, count: Int): List<TimeSlot> {
        val now = Clock.System.now()
        return (1..count).map { index ->
            val start = now.plusHours(index.toLong() * 2)
            TimeSlot(
                startTime = start,
                endTime = start.plusHours(1),
                isAvailable = true,
                providerId = providerId,
            )
        }
    }

    private fun Instant.plusHours(hours: Long): Instant {
        return Instant.fromEpochMilliseconds(
            toEpochMilliseconds() + hours * 60L * 60L * 1000L,
        )
    }

    private fun LocalDate.plusDays(days: Int): LocalDate {
        val epochDays = this.toEpochDays() + days
        return kotlinx.datetime.LocalDate.fromEpochDays(epochDays)
    }

    private fun LocalDate.minusDays(days: Int): LocalDate {
        val epochDays = this.toEpochDays() - days
        return kotlinx.datetime.LocalDate.fromEpochDays(epochDays)
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
        var getAvailableSlotsResult: Result<List<TimeSlot>> = Result.success(emptyList())
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
        var lastGetAvailableSlotsProviderId: String? = null
        var lastGetAvailableSlotsFromDate: LocalDate? = null
        var lastGetAvailableSlotsToDate: LocalDate? = null
        var lastGetAvailableSlotsServiceIds: List<String>? = null

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
        ): Result<List<TimeSlot>> {
            getAvailableSlotsCallCount++
            lastGetAvailableSlotsProviderId = providerId
            lastGetAvailableSlotsFromDate = fromDate
            lastGetAvailableSlotsToDate = toDate
            lastGetAvailableSlotsServiceIds = serviceIds
            return getAvailableSlotsResult
        }

        override suspend fun getProviderServices(providerId: String): Result<List<com.aggregateservice.feature.booking.domain.model.BookingService>> {
            getProviderServicesCallCount++
            return getProviderServicesResult
        }
    }
}
