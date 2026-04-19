package com.aggregateservice.feature.reviews.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.utils.ValidationRule
import com.aggregateservice.feature.reviews.domain.model.ProviderReply
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.repository.ReviewsRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for CreateReviewUseCase.
 */
class CreateReviewUseCaseTest {
    private lateinit var createReviewUseCase: CreateReviewUseCase
    private lateinit var mockRepository: MockReviewsRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockReviewsRepository()
        createReviewUseCase = CreateReviewUseCase(mockRepository)
    }

    // ========== Success Cases ==========

    @Test
    fun `should create review successfully`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 5
            val comment = "Great service!"
            val expectedReview = createTestReview(bookingId, rating)

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.success(expectedReview)

            // Act
            val result = createReviewUseCase(bookingId, rating, comment)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(1, mockRepository.canReviewBookingCallCount)
            assertEquals(bookingId, mockRepository.lastCanReviewBookingId)
            assertEquals(1, mockRepository.createReviewCallCount)
            assertEquals(bookingId, mockRepository.lastCreateReviewBookingId)
            assertEquals(rating, mockRepository.lastCreateReviewRating)
            assertEquals(comment, mockRepository.lastCreateReviewComment)
        }

    @Test
    fun `should create review with null comment`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 4

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.success(createTestReview(bookingId, rating))

            // Act
            val result = createReviewUseCase(bookingId, rating, null)

            // Assert
            assertTrue(result.isSuccess)
            assertNull(mockRepository.lastCreateReviewComment)
        }

    @Test
    fun `should create review with minimum rating`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = CreateReviewUseCase.MIN_RATING

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.success(createTestReview(bookingId, rating))

            // Act
            val result = createReviewUseCase(bookingId, rating, "OK")

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `should create review with maximum rating`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = CreateReviewUseCase.MAX_RATING

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.success(createTestReview(bookingId, rating))

            // Act
            val result = createReviewUseCase(bookingId, rating, "Excellent!")

            // Assert
            assertTrue(result.isSuccess)
        }

    @Test
    fun `should trim comment whitespace`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 4
            val untrimmedComment = "  Great service!  "

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.success(createTestReview(bookingId, rating))

            // Act
            val result = createReviewUseCase(bookingId, rating, untrimmedComment)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals("Great service!", mockRepository.lastCreateReviewComment)
        }

    // ========== Validation Error Cases ==========

    @Test
    fun `should fail when bookingId is blank`() =
        runTest {
            // Arrange
            val bookingId = "   "
            val rating = 5

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("bookingId", error.field)
            assertEquals(ValidationRule.Required, error.rule)
        }

    @Test
    fun `should fail when rating is below minimum`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 0

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("rating", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
            assertEquals(CreateReviewUseCase.MIN_RATING, error.parameters["min"])
            assertEquals(CreateReviewUseCase.MAX_RATING, error.parameters["max"])
        }

    @Test
    fun `should fail when rating is above maximum`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 6

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("rating", error.field)
            assertEquals(ValidationRule.TooLow, error.rule)
        }

    @Test
    fun `should fail when booking cannot be reviewed`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 5

            mockRepository.canReviewBookingResult = Result.success(false)

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertIs<AppError.FormValidation>(error)
            assertEquals("bookingId", error.field)
            assertEquals(ValidationRule.InvalidValue, error.rule)
        }

    // ========== Repository Error Cases ==========

    @Test
    fun `should return error when canReviewBooking repository fails`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 5
            val expectedError = AppError.NetworkError(503, "Service Unavailable")

            mockRepository.canReviewBookingResult = Result.failure(expectedError)

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when createReview repository fails`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 5
            val expectedError = AppError.NetworkError(500, "Internal Server Error")

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.failure(expectedError)

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedError, result.exceptionOrNull())
        }

    @Test
    fun `should return error when repository returns conflict`() =
        runTest {
            // Arrange
            val bookingId = "booking-123"
            val rating = 5

            mockRepository.canReviewBookingResult = Result.success(true)
            mockRepository.createReviewResult = Result.failure(AppError.Conflict("Review already exists"))

            // Act
            val result = createReviewUseCase(bookingId, rating)

            // Assert
            assertTrue(result.isFailure)
            assertIs<AppError.Conflict>(result.exceptionOrNull())
        }

    // ========== Helper Methods ==========

    private fun createTestReview(bookingId: String, rating: Int): Review {
        val now = Clock.System.now()
        return Review(
            id = "review-123",
            bookingId = bookingId,
            providerId = "provider-456",
            providerName = "Test Provider",
            clientId = "client-789",
            clientName = "Test Client",
            rating = rating,
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
