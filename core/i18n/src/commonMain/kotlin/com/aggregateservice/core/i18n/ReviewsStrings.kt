package com.aggregateservice.core.i18n

/**
 * Reviews string resources.
 */
public object ReviewsStrings {
    public val EN: Map<String, String> =
        mapOf(
            "reviews_title" to "Reviews",
            "reviews_write" to "Write a review",
            "reviews_rating" to "Rating",
            "reviews_none" to "No reviews yet",
            "reviews_helpful" to "Helpful",
            "reviews_report" to "Report",
            "plurals_reviews_count" to "{count} reviews",
        )

    public val RU: Map<String, String> =
        mapOf(
            "reviews_title" to "Отзывы",
            "reviews_write" to "Написать отзыв",
            "reviews_rating" to "Рейтинг",
            "reviews_none" to "Отзывов пока нет",
            "reviews_helpful" to "Полезно",
            "reviews_report" to "Пожаловаться",
            "plurals_reviews_count" to "{count} отзывов",
        )

    public val HE: Map<String, String> =
        mapOf(
            "reviews_title" to "ביקורות",
            "reviews_write" to "כתוב ביקורת",
            "reviews_rating" to "דירוג",
            "reviews_none" to "אין ביקורות עדיין",
            "reviews_helpful" to "מועיל",
            "reviews_report" to "דווח",
            "plurals_reviews_count" to "{count} ביקורות",
        )
}
