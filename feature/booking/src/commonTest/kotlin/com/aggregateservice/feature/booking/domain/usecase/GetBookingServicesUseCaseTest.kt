package com.aggregateservice.feature.booking.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.repository.BookingRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for GetBookingServicesUseCase.
 */
class GetBookingServicesUseCaseTest {
    private lateinit var getBookingServicesUseCase: GetBookingServicesUseCase
    private lateinit var mockRepository: MockBookingRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockBookingRepository()
        getBookingServicesUseCase = GetBookingServicesUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return services on successful fetch`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedServices = createTestServices(3)
            mockRepository.getProviderServicesResult = Result.success(expectedServices)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val services = result.getOrNull()!!
            assertEquals(3, services.size)
        }

    @Test
    fun `should return empty list when provider has no services`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            mockRepository.getProviderServicesResult = Result.success(emptyList())

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.isEmpty())
        }

    @Test
    fun `should call repository with correct providerId`() =
        runTest {
            // Arrange
            val providerId = "provider-456"
            mockRepository.getProviderServicesResult = Result.success(emptyList())

            // Act
            getBookingServicesUseCase(providerId)

            // Assert
            assertEquals(1, mockRepository.getProviderServicesCallCount)
            assertEquals(providerId, mockRepository.lastGetProviderServicesProviderId)
        }

    @Test
    fun `should return services with correct properties`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedServices = listOf(
                BookingService(
                    id = "service-1",
                    name = "Haircut",
                    description = "Classic haircut service",
                    price = 100.0,
                    currency = "ILS",
                    durationMinutes = 30,
                    isCombinable = true,
                ),
            )
            mockRepository.getProviderServicesResult = Result.success(expectedServices)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val service = result.getOrNull()!!.first()
            assertEquals("service-1", service.id)
            assertEquals("Haircut", service.name)
            assertEquals("Classic haircut service", service.description)
            assertEquals(100.0, service.price)
            assertEquals("ILS", service.currency)
            assertEquals(30, service.durationMinutes)
            assertTrue(service.isCombinable)
        }

    @Test
    fun `should return service with formatted price`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val services = listOf(
                BookingService(
                    id = "service-1",
                    name = "Haircut",
                    price = 150.0,
                    currency = "ILS",
                    durationMinutes = 30,
                ),
            )
            mockRepository.getProviderServicesResult = Result.success(services)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val service = result.getOrNull()!!.first()
            assertEquals("150 ILS", service.formattedPrice)
        }

    @Test
    fun `should return service with formatted duration`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val services = listOf(
                BookingService(
                    id = "service-1",
                    name = "Hair Treatment",
                    price = 200.0,
                    currency = "ILS",
                    durationMinutes = 90,
                ),
            )
            mockRepository.getProviderServicesResult = Result.success(services)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val service = result.getOrNull()!!.first()
            assertEquals("90 min", service.formattedDuration)
        }

    @Test
    fun `should return service with short description`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val longDescription = "A".repeat(150)
            val services = listOf(
                BookingService(
                    id = "service-1",
                    name = "Full Service",
                    description = longDescription,
                    price = 300.0,
                    currency = "ILS",
                    durationMinutes = 120,
                ),
            )
            mockRepository.getProviderServicesResult = Result.success(services)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val service = result.getOrNull()!!.first()
            assertEquals(100, service.shortDescription!!.length)
            assertEquals("A".repeat(100), service.shortDescription)
        }

    @Test
    fun `should return service with null description`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val services = listOf(
                BookingService(
                    id = "service-1",
                    name = "Simple Service",
                    description = null,
                    price = 50.0,
                    currency = "ILS",
                    durationMinutes = 15,
                ),
            )
            mockRepository.getProviderServicesResult = Result.success(services)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val service = result.getOrNull()!!.first()
            assertTrue(service.description == null)
            assertTrue(service.shortDescription == null)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when providerId is blank`() =
        runTest {
            // Arrange
            val providerId = "   "

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("providerId", error.field)
            assertEquals(ValidationRule.Required, error.rule)
        }

    @Test
    fun `should fail when providerId is empty`() =
        runTest {
            // Arrange
            val providerId = ""

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("providerId", error.field)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when provider not found`() =
        runTest {
            // Arrange
            val providerId = "nonexistent"
            mockRepository.getProviderServicesResult = Result.failure(AppError.NotFound)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository fails with network error`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedError = AppError.NetworkError(503, "Service Unavailable")
            mockRepository.getProviderServicesResult = Result.failure(expectedError)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NetworkError>(result.exceptionOrNull())
            assertEquals(503, (result.exceptionOrNull() as AppError.NetworkError).code)
        }

    @Test
    fun `should return error when repository fails with unauthorized`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            mockRepository.getProviderServicesResult = Result.failure(AppError.Unauthorized)

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Unauthorized>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository throws unexpected exception`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            mockRepository.getProviderServicesResult = Result.failure(RuntimeException("Unexpected"))

            // Act
            val result = getBookingServicesUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() != null)
        }

    // ========== Helper Methods ==========

    private fun createTestServices(count: Int): List<BookingService> {
        return (1..count).map { index ->
            BookingService(
                id = "service-$index",
                name = "Service $index",
                description = "Description for service $index",
                price = index * 50.0,
                currency = "ILS",
                durationMinutes = index * 15,
                isCombinable = index % 2 == 0,
            )
        }
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
        var getProviderServicesResult: Result<List<BookingService>> = Result.success(emptyList())

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
        var lastGetProviderServicesProviderId: String? = null

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
        ): Result<List<com.aggregateservice.feature.booking.domain.model.TimeSlot>> {
            getAvailableSlotsCallCount++
            return getAvailableSlotsResult
        }

        override suspend fun getProviderServices(providerId: String): Result<List<BookingService>> {
            getProviderServicesCallCount++
            lastGetProviderServicesProviderId = providerId
            return getProviderServicesResult
        }
    }
}
