package com.aggregateservice.core.navigation

/**
 * Базовый интерфейс для всех экранов приложения.
 *
 * Используется Voyager для типобезопасной навигации.
 */
sealed interface Screen {
    /**
     * Уникальный маршрут экрана.
     */
    val route: String
}

/**
 * Auth экраны.
 */
sealed class AuthScreen(override val route: String) : Screen {
    /**
     * Экран входа.
     */
    data object Login : AuthScreen("auth/login")

    /**
     * Экран регистрации.
     */
    data object Register : AuthScreen("auth/register")

    /**
     * Экран восстановления пароля.
     */
    data object ForgotPassword : AuthScreen("auth/forgot_password")
}

/**
 * Главные экраны приложения.
 */
sealed class MainScreen(override val route: String) : Screen {
    /**
     * Экран каталога услуг.
     */
    data object Catalog : MainScreen("catalog")

    /**
     * Экран бронирования.
     */
    data object Booking : MainScreen("booking")

    /**
     * Экран профиля.
     */
    data object Profile : MainScreen("profile")

    /**
     * Экран избранного.
     */
    data object Favorites : MainScreen("favorites")

    /**
     * Экран отзывов.
     */
    data object Reviews : MainScreen("reviews")

    /**
     * Экран расписания.
     */
    data object Schedule : MainScreen("schedule")
}
