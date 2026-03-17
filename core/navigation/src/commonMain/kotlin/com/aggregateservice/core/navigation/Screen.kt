package com.aggregateservice.core.navigation

import cafe.adriel.voyager.core.screen.Screen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOOKINGS = "bookings"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val PROVIDER_DETAIL = "provider/{providerId}"
    const val BOOKING_FLOW = "booking/{providerId}"
    const val SETTINGS = "settings"
}

interface AppScreen : Screen {
    val route: String
}
