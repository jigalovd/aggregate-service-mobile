package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for GetProviderReviewsUseCase.
 */
class GetProviderReviewsUseCaseTest {
    private lateinit var getProviderReviewsUseCase: GetProviderReviewsUseCase
    private lateinit var mockRepository: MockReviewsRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockReviewsRepository()
        getProviderReviewsUseCase = GetProviderReviewsUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should return reviews on successful fetch`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 1
            val pageSize = 20
            val expectedReviews = listOf(
                createTestReview("review-1"),
                createTestReview("review-2"),
            )
            mockRepository.getProviderReviewsResult = Result.success(expectedReviews)

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()!!.size)
        }

    @Test
    fun `should call repository with correct parameters`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 2
            val pageSize = 10

            mockRepository.getProviderReviewsResult = Result.success(emptyList())

            // Act
            getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertEquals(1, mockRepository.getProviderReviewsCallCount)
            assertEquals(providerId, mockRepository.lastGetProviderReviewsProviderId)
            assertEquals(page, mockRepository.lastGetProviderReviewsPage)
            assertEquals(pageSize, mockRepository.lastGetProviderReviewsPageSize)
        }

    @Test
    fun `should use default pagination values`() =
        runTest {
            // Arrange
            val providerId = "provider-123"

            mockRepository.getProviderReviewsResult = Result.success(emptyList())

            // Act
            getProviderReviewsUseCase(providerId)

            // Assert
            assertEquals(GetProviderReviewsUseCase.DEFAULT_PAGE, mockRepository.lastGetProviderReviewsPage)
            assertEquals(GetProviderReviewsUseCase.DEFAULT_PAGE_SIZE, mockRepository.lastGetProviderReviewsPageSize)
        }

    @Test
    fun `should return reviews with pagination info`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 3
            val pageSize = 5
            val expectedReviews = (1..5).map { createTestReview("review-$it") }

            mockRepository.getProviderReviewsResult = Result.success(expectedReviews)

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isSuccess)
            val reviews = result.getOrNull()!!
            assertEquals(5, reviews.size)
            assertEquals("review-1", reviews[0].id)
            assertEquals("review-5", reviews[4].id)
        }

    @Test
    fun `should return empty list when no reviews`() =
        runTest {
            // Arrange
            val providerId = "provider-123"

            mockRepository.getProviderReviewsResult = Result.success(emptyList())

            // Act
            val result = getProviderReviewsUseCase(providerId)

            // Assert
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.isEmpty())
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when providerId is blank`() =
        runTest {
            // Arrange
            val providerId = "   "
            val page = 1
            val pageSize = 20

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(error)
            assertTrue(error.message!!.contains("Provider ID"))
        }

    @Test
    fun `should fail when page is below minimum`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 0
            val pageSize = 20

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(error)
            assertTrue(error.message!!.contains("Page"))
        }

    @Test
    fun `should fail when pageSize is below minimum`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 1
            val pageSize = 0

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(error)
            assertTrue(error.message!!.contains("Page size"))
        }

    @Test
    fun `should fail when pageSize exceeds maximum`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 1
            val pageSize = 101

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(error)
            assertTrue(error.message!!.contains("Page size"))
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when repository fails with network error`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 1
            val pageSize = 20
            val expectedError = com.aggregateservice.core.network.AppError.NetworkError(503, "Service Unavailable")

            mockRepository.getProviderReviewsResult = Result.failure(expectedError)

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository fails with unauthorized`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 1
            val pageSize = 20
            val expectedError = com.aggregateservice.core.network.AppError.Unauthorized

            mockRepository.getProviderReviewsResult = Result.failure(expectedError)

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            assertIs<com.aggregateservice.core.network.AppError.Unauthorized>(result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository throws unexpected exception`() =
        runTest {
            // Arrange
            val providerId = "provider-123"
            val page = 1
            val pageSize = 20

            mockRepository.getProviderReviewsResult = Result.failure(RuntimeException("Unexpected"))

            // Act
            val result = getProviderReviewsUseCase(providerId, page, pageSize)

            // Assert
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() != null)
        }

    // ========== Helper Methods ==========

    private fun createTestReview(id: String): Review {
        val now = Clock.System.now()
        return Review(
            id = id,
            bookingId = "booking-123",
            providerId = "provider-123",
            providerName = "Test Provider",
            clientId = "client-456",
            clientName = "Test Client",
            rating = 5,
            comment = "Great service!",
            providerReply = null,
            createdAt = now,
        )
    }

    /**
     * Mock implementation of ReviewsRepository for testing.
     */
    private class MockReviewsRepository : ReviewsRepository {
        // Result properties
        var getProviderReviewsResult: Result<List<Review>> = Result.success(emptyList())
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
        var createReviewResult: Result<Review> = Result.success(
            Review(
                id = "review-123",
                bookingId = "",
                providerId = "provider-456",
                providerName = "Test Provider",
                clientId = "client-789",
                clientName = "Test Client",
                rating = 5,
                comment = null,
                providerReply = null,
                createdAt = Clock.System.now(),
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
        ): Result<List<Review>> {
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
        ): Result<Review> {
            createReviewCallCount++
            lastCreateReviewBookingId = bookingId
            lastCreateReviewRating = rating
            lastCreateReviewComment = comment
            return createReviewResult
        }
    }
}
