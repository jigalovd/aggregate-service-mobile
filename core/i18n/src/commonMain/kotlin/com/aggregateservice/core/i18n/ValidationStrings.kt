package com.aggregateservice.core.i18n

/**
 * Validation string resources.
 */
public object ValidationStrings {
    public val EN: Map<String, String> =
        mapOf(
            "validation.required" to "This field is required",
            "validation.too_short" to "Must be at least %s characters",
            "validation.too_long" to "Must be at most %s characters",
            "validation.invalid_format" to "Invalid format",
            "validation.too_low" to "Must be at least %s",
            "validation.too_high" to "Must be at most %s",
            "validation.non_negative" to "Must be non-negative",
            "validation.not_blank" to "Cannot be blank",
            "validation.not_empty" to "Cannot be empty",
            "validation.invalid_value" to "Invalid value",
        )

    public val RU: Map<String, String> =
        mapOf(
            "validation.required" to "Обязательное поле",
            "validation.too_short" to "Минимум %s символов",
            "validation.too_long" to "Максимум %s символов",
            "validation.invalid_format" to "Неверный формат",
            "validation.too_low" to "Минимальное значение: %s",
            "validation.too_high" to "Максимальное значение: %s",
            "validation.non_negative" to "Значение должно быть неотрицательным",
            "validation.not_blank" to "Поле не может быть пустым",
            "validation.not_empty" to "Поле не может быть пустым",
            "validation.invalid_value" to "Неверное значение",
        )

    public val HE: Map<String, String> =
        mapOf(
            "validation.required" to "שדה חובה",
            "validation.too_short" to "חייב להיות לפחות %s תווים",
            "validation.too_long" to "חייב להיות לכל היותר %s תווים",
            "validation.invalid_format" to "פורמט לא תקין",
            "validation.too_low" to "חייב להיות לפחות %s",
            "validation.too_high" to "חייב להיות לכל היותר %s",
            "validation.non_negative" to "חייב להיות לא שלילי",
            "validation.not_blank" to "לא יכול להיות ריק",
            "validation.not_empty" to "לא יכול להיות ריק",
            "validation.invalid_value" to "ערך לא תקין",
        )
}
