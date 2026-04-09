package com.aggregateservice.feature.reviews.domain.model

/**
 * Statistics for a provider's reviews.
 *
 * **Note:** Domain models must NOT import Compose/Android dependencies.
 *
 * @property providerId ID of the provider
 * @property averageRating Average rating (1.0 - 5.0)
 * @property totalReviews Total number of reviews
 * @property ratingDistribution Distribution of ratings: {5: count, 4: count, ...}
 */
data class ReviewStats(
    val providerId: String,
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>,
) {
    /**
     * Formatted average rating as "X.X".
     */
    val formattedAverageRating: String
        get() = "%.1f".format(averageRating)

    /**
     * Percentage of reviews for a given rating.
     */
    fun getPercentageForRating(rating: Int): Double {
        if (totalReviews == 0) return 0.0
        val count = ratingDistribution[rating] ?: 0
        return (count * 100.0) / totalReviews
    }

    /**
     * Count for a specific rating (0 if none).
     */
    fun getCountForRating(rating: Int): Int = ratingDistribution[rating] ?: 0

    companion object {
        val EMPTY =
            ReviewStats(
                providerId = "",
                averageRating = 0.0,
                totalReviews = 0,
                ratingDistribution = emptyMap(),
            )
    }
}
