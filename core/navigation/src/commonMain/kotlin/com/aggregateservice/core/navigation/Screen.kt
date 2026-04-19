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
sealed class AuthScreen(
    override val route: String,
) : Screen {
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
 * Provider экраны (доступны только с PROVIDER ролью).
 */
sealed class ProviderScreen(
    override val route: String,
) : Screen {
    /**
     * Provider Dashboard - главный экран провайдера.
     */
    data object Dashboard : ProviderScreen("provider/dashboard")

    /**
     * Provider Onboarding - 3-шаговый мастер регистрации.
     */
    data object Onboarding : ProviderScreen("provider/onboarding")

    /**
     * Provider Profile - профиль провайдера.
     */
    data object Profile : ProviderScreen("provider/profile")

    /**
     * Provider Bookings - бронирования провайдера.
     */
    data object Bookings : ProviderScreen("provider/bookings")

    /**
     * Provider Portfolio - портфолио провайдера.
     */
    data object Portfolio : ProviderScreen("provider/portfolio")
}

/**
 * Главные экраны приложения.
 */
sealed class MainScreen(
    override val route: String,
) : Screen {
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
