package com.aggregateservice.core.i18n

/**
 * Error string resources.
 */
public object ErrorStrings {
    public val EN: Map<String, String> =
        mapOf(
            "error_network" to "Network error. Please check your connection.",
            "error_server" to "Server error. Please try again later.",
            "error_unknown" to "An unexpected error occurred.",
            "error_validation" to "Please check your input.",
            "error_unauthorized" to "You are not authorized. Please login.",
            "error_rate_limit" to "Rate limit exceeded. Try again in %s seconds",
            "error_account_locked" to "Account locked until %s",
        )

    public val RU: Map<String, String> =
        mapOf(
            "error_network" to "Ошибка сети. Проверьте подключение.",
            "error_server" to "Ошибка сервера. Попробуйте позже.",
            "error_unknown" to "Произошла непредвиденная ошибка.",
            "error_validation" to "Проверьте введённые данные.",
            "error_unauthorized" to "Вы не авторизованы. Пожалуйста, войдите.",
            "error_rate_limit" to "Превышен лимит запросов. Повторите через %s секунд",
            "error_account_locked" to "Аккаунт заблокирован до %s",
        )

    public val HE: Map<String, String> =
        mapOf(
            "error_network" to "שגיאת רשת. בדוק את החיבור.",
            "error_server" to "שגיאת שרת. נסה מאוחר יותר.",
            "error_unknown" to "אירעה שגיאה בלתי צפויה.",
            "error_validation" to "בדוק את הנתונים שהוזנו.",
            "error_unauthorized" to "אינך מורשה. אנא התחבר.",
            "error_rate_limit" to "חריגת מגבלת בקשות. נסה שוב בעוד %s שניות",
            "error_account_locked" to "החשבון נעול עד %s",
        )
}
