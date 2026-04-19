package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for CanReviewBookingUseCase.
 */
class CanReviewBookingUseCaseTest {
    private lateinit var canReviewBookingUseCase: CanReviewBookingUseCase
    private lateinit var mockRepository: MockReviewsRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockReviewsRepository()
        canReviewBookingUseCase = CanReviewBookingUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return true when booking can be reviewed`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"

            mockRepository.canReviewBookingResult = Result.success(true)

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(true, result.getOrNull())
            assertEquals(1, mockRepository.canReviewBookingCallCount)
            assertEquals(bookingId, mockRepository.lastCanReviewBookingId)
        }

    @Test
    fun `should return false when booking already has review`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"

            mockRepository.canReviewBookingResult = Result.success(false)

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(false, result.getOrNull())
        }

    @Test
    fun `should return false when booking is not completed`() =
        runTest {
            // Arrange
            val bookingId = "booking-456"

            mockRepository.canReviewBookingResult = Result.success(false)

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(false, result.getOrNull())
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when bookingId is blank`() =
        runTest {
            // Arrange
            val bookingId = "   "

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(error)
            assertTrue(error.message!!.contains("Booking ID"))
        }

    @Test
    fun `should fail when bookingId is empty`() =
        runTest {
            // Arrange
            val bookingId = ""

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<IllegalArgumentException>(result.exceptionOrNull())
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository fails with network error`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.canReviewBookingResult = Result.failure(expectedError)

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository fails with unauthorized`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val expectedError = AppError.Unauthorized

            mockRepository.canReviewBookingResult = Result.failure(expectedError)

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Unauthorized>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository fails with not found`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val expectedError = AppError.NotFound

            mockRepository.canReviewBookingResult = Result.failure(expectedError)

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository throws unexpected exception`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"

            mockRepository.canReviewBookingResult = Result.failure(RuntimeException("Unexpected"))

            // Act
            val result = canReviewBookingUseCase(bookingId)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() != null)
        }

    // ========== Helper Methods ==========

    /**
     * Mock implementation of ReviewsRepository for testing.
     */
    private class MockReviewsRepository : ReviewsRepository {
        // Result properties
        var getProviderReviewsResult: Result<List<com.aggregateservice.feature.reviews.domain.model.Review>> =
            Result.success(emptyList())
        var getReviewStatsResult: Result<com.aggregateservice.feature.reviews.domain.model.ReviewStats> =
            Result.success(
                com.aggregateservice.feature.reviews.domain.model.ReviewStats(
                    providerId = "",
                    averageRating = 0.0,
                    totalReviews = 0,
                    ratingDistribution = emptyMap(),
                ),
            )
        var canReviewBookingResult: Result<Boolean> = Result.success(true)
        var createReviewResult: Result<com.aggregateservice.feature.reviews.domain.model.Review> =
            Result.success(
                com.aggregateservice.feature.reviews.domain.model.Review(
                    id = "review-123",
                    bookingId = "",
                    providerId = "provider-456",
                    providerName = "Test Provider",
                    clientId = "client-789",
                    clientName = "Test Client",
                    rating = 5,
                    comment = null,
                    providerReply = null,
                    createdAt = kotlinx.datetime.Clock.System.now(),
                ),
            )

        // Call tracking
        var getProviderReviewsCallCount = 0
        var getReviewStatsCallCount = 0
        var canReviewBookingCallCount = 0
        var createReviewCallCount = 0

        // Last call parameters
        var lastGetProviderReviewsProviderId: String? = null
        var lastGetProviderReviewsPage: Int? = null
        var lastGetProviderReviewsPageSize: Int? = null
        var lastGetReviewStatsProviderId: String? = null
        var lastCanReviewBookingId: String? = null
        var lastCreateReviewBookingId: String? = null
        var lastCreateReviewRating: Int? = null
        var lastCreateReviewComment: String? = null

        override suspend fun getProviderReviews(
            providerId: String,
            page: Int,
            pageSize: Int,
        ): Result<List<com.aggregateservice.feature.reviews.domain.model.Review>> {
            getProviderReviewsCallCount++
            lastGetProviderReviewsProviderId = providerId
            lastGetProviderReviewsPage = page
            lastGetProviderReviewsPageSize = pageSize
            return getProviderReviewsResult
        }

        override suspend fun getReviewStats(providerId: String): Result<com.aggregateservice.feature.reviews.domain.model.ReviewStats> {
            getReviewStatsCallCount++
            lastGetReviewStatsProviderId = providerId
            return getReviewStatsResult
        }

        override suspend fun canReviewBooking(bookingId: String): Result<Boolean> {
            canReviewBookingCallCount++
            lastCanReviewBookingId = bookingId
            return canReviewBookingResult
        }

        override suspend fun createReview(
            bookingId: String,
            rating: Int,
            comment: String?,
        ): Result<com.aggregateservice.feature.reviews.domain.model.Review> {
            createReviewCallCount++
            lastCreateReviewBookingId = bookingId
            lastCreateReviewRating = rating
            lastCreateReviewComment = comment
            return createReviewResult
        }
    }
}
