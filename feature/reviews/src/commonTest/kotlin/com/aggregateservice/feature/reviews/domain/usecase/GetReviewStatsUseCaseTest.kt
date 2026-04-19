package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.reviews.domain.model.ReviewStats
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for GetReviewStatsUseCase.
 */
class GetReviewStatsUseCaseTest {
    private lateinit var getReviewStatsUseCase: GetReviewStatsUseCase
    private lateinit var mockRepository: MockReviewsRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockReviewsRepository()
        getReviewStatsUseCase = GetReviewStatsUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return stats on successful fetch`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedStats = createTestReviewStats(providerId)

            mockRepository.getReviewStatsResult = Result.success(expectedStats)

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(providerId, result.getOrNull()!!.providerId)
        }

    @Test
    fun `should call repository with correct providerId`() =
        runTest {
            // Arrange
            val providerId = "provider-456"

            mockRepository.getReviewStatsResult = Result.success(createTestReviewStats(providerId))

            // Act
            getReviewStatsUseCase(providerId)

            // Assert
            assertEquals(1, mockRepository.getReviewStatsCallCount)
            assertEquals(providerId, mockRepository.lastGetReviewStatsProviderId)
        }

    @Test
    fun `should return stats with all properties`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedStats = ReviewStats(
                providerId = providerId,
                averageRating = 4.5,
                totalReviews = 100,
                ratingDistribution = mapOf(
                    5 to 50,
                    4 to 30,
                    3 to 10,
                    2 to 7,
                    1 to 3,
                ),
            )

            mockRepository.getReviewStatsResult = Result.success(expectedStats)

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val stats = result.getOrNull()!!
            assertEquals(providerId, stats.providerId)
            assertEquals(4.5, stats.averageRating)
            assertEquals(100, stats.totalReviews)
            assertEquals(50, stats.ratingDistribution[5])
        }

    @Test
    fun `should return empty stats when no reviews`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val emptyStats = ReviewStats.EMPTY

            mockRepository.getReviewStatsResult = Result.success(emptyStats)

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val stats = result.getOrNull()!!
            assertEquals(0.0, stats.averageRating)
            assertEquals(0, stats.totalReviews)
            assertTrue(stats.ratingDistribution.isEmpty())
        }

    @Test
    fun `should calculate correct rating percentages`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val stats = ReviewStats(
                providerId = providerId,
                averageRating = 4.0,
                totalReviews = 100,
                ratingDistribution = mapOf(
                    5 to 40,
                    4 to 30,
                    3 to 20,
                    2 to 10,
                    1 to 0,
                ),
            )

            mockRepository.getReviewStatsResult = Result.success(stats)

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            val returnedStats = result.getOrNull()!!
            assertEquals(40.0, returnedStats.getPercentageForRating(5))
            assertEquals(30.0, returnedStats.getPercentageForRating(4))
            assertEquals(20.0, returnedStats.getPercentageForRating(3))
            assertEquals(0.0, returnedStats.getPercentageForRating(1))
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when providerId is blank`() =
        runTest {
            // Arrange
            val providerId = "   "

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(error)
            assertTrue(error.message!!.contains("Provider ID"))
        }

    @Test
    fun `should fail when providerId is empty`() =
        runTest {
            // Arrange
            val providerId = ""

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<IllegalArgumentException>(result.exceptionOrNull())
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository fails with network error`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.getReviewStatsResult = Result.failure(expectedError)

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository fails with not found`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val expectedError = AppError.NotFound

            mockRepository.getReviewStatsResult = Result.failure(expectedError)

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.NotFound>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository throws unexpected exception`() =
        runTest {
            // Arrange
            val providerId = "provider-123"

            mockRepository.getReviewStatsResult = Result.failure(RuntimeException("Unexpected"))

            // Act
            val result = getReviewStatsUseCase(providerId)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() != null)
        }

    // ========== Helper Methods ==========

    private fun createTestReviewStats(providerId: String): ReviewStats {
        return ReviewStats(
            providerId = providerId,
            averageRating = 4.2,
            totalReviews = 50,
            ratingDistribution = mapOf(
                5 to 25,
                4 to 15,
                3 to 7,
                2 to 2,
                1 to 1,
            ),
        )
    }

    /**
     * Mock implementation of ReviewsRepository for testing.
     */
    private class MockReviewsRepository : ReviewsRepository {
        // Result properties
        var getProviderReviewsResult: Result<List<com.aggregateservice.feature.reviews.domain.model.Review>> =
            Result.success(emptyList())
        var getReviewStatsResult: Result<ReviewStats> = Result.success(
            ReviewStats(
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

        override suspend fun getReviewStats(providerId: String): Result<ReviewStats> {
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
