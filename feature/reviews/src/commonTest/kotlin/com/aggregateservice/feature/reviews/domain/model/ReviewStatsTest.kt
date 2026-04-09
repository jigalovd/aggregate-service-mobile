package com.aggregateservice.feature.reviews.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ReviewStats].
 */
class ReviewStatsTest {
    /**
     * Test: percentageForRating returns correct double value.
     */
    @Test
    fun percentageForRating_returnsCorrectDouble() {
        val stats =
            ReviewStats(
                providerId = "test",
                averageRating = 4.5,
                totalReviews = 3,
                ratingDistribution = mapOf(5 to 1, 4 to 1, 3 to 1),
            )
        assertEquals(33.33, stats.getPercentageForRating(5), 0.01)
        assertEquals(33.33, stats.getPercentageForRating(4), 0.01)
        assertEquals(33.33, stats.getPercentageForRating(3), 0.01)
    }
}
