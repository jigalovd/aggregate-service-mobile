package com.aggregateservice.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [Screen] sealed interface and its implementations.
 * Verifies route constants and type hierarchy.
 */
class ScreenTest {

    // --- AuthScreen routes ---

    @Test
    fun `AuthScreen Login has correct route`() {
        val screen = AuthScreen.Login

        assertEquals("auth/login", screen.route)
    }

    @Test
    fun `AuthScreen Register has correct route`() {
        val screen = AuthScreen.Register

        assertEquals("auth/register", screen.route)
    }

    @Test
    fun `AuthScreen ForgotPassword has correct route`() {
        val screen = AuthScreen.ForgotPassword

        assertEquals("auth/forgot_password", screen.route)
    }

    // --- MainScreen routes ---

    @Test
    fun `MainScreen Catalog has correct route`() {
        val screen = MainScreen.Catalog

        assertEquals("catalog", screen.route)
    }

    @Test
    fun `MainScreen Booking has correct route`() {
        val screen = MainScreen.Booking

        assertEquals("booking", screen.route)
    }

    @Test
    fun `MainScreen Profile has correct route`() {
        val screen = MainScreen.Profile

        assertEquals("profile", screen.route)
    }

    @Test
    fun `MainScreen Favorites has correct route`() {
        val screen = MainScreen.Favorites

        assertEquals("favorites", screen.route)
    }

    @Test
    fun `MainScreen Reviews has correct route`() {
        val screen = MainScreen.Reviews

        assertEquals("reviews", screen.route)
    }

    @Test
    fun `MainScreen Schedule has correct route`() {
        val screen = MainScreen.Schedule

        assertEquals("schedule", screen.route)
    }

    // --- Type hierarchy tests ---

    @Test
    fun `AuthScreen instances are Screen type`() {
        val login: Screen = AuthScreen.Login
        val register: Screen = AuthScreen.Register
        val forgotPassword: Screen = AuthScreen.ForgotPassword

        assertEquals(true, login is Screen)
        assertEquals(true, register is Screen)
        assertEquals(true, forgotPassword is Screen)
    }

    @Test
    fun `MainScreen instances are Screen type`() {
        val catalog: Screen = MainScreen.Catalog
        val booking: Screen = MainScreen.Booking
        val profile: Screen = MainScreen.Profile

        assertEquals(true, catalog is Screen)
        assertEquals(true, booking is Screen)
        assertEquals(true, profile is Screen)
    }

    // --- Route uniqueness tests ---

    @Test
    fun `All auth routes are unique`() {
        val routes = setOf(
            AuthScreen.Login.route,
            AuthScreen.Register.route,
            AuthScreen.ForgotPassword.route,
        )

        assertEquals(3, routes.size)
    }

    @Test
    fun `All main routes are unique`() {
        val routes = setOf(
            MainScreen.Catalog.route,
            MainScreen.Booking.route,
            MainScreen.Profile.route,
            MainScreen.Favorites.route,
            MainScreen.Reviews.route,
            MainScreen.Schedule.route,
        )

        assertEquals(6, routes.size)
    }

    @Test
    fun `Auth routes do not overlap with main routes`() {
        val authRoutes = setOf(
            AuthScreen.Login.route,
            AuthScreen.Register.route,
            AuthScreen.ForgotPassword.route,
        )
        val mainRoutes = setOf(
            MainScreen.Catalog.route,
            MainScreen.Booking.route,
            MainScreen.Profile.route,
            MainScreen.Favorites.route,
            MainScreen.Reviews.route,
            MainScreen.Schedule.route,
        )

        assertEquals(0, authRoutes.intersect(mainRoutes).size)
    }

    // --- Route prefix tests ---

    @Test
    fun `All auth routes start with auth prefix`() {
        assertEquals(true, AuthScreen.Login.route.startsWith("auth/"))
        assertEquals(true, AuthScreen.Register.route.startsWith("auth/"))
        assertEquals(true, AuthScreen.ForgotPassword.route.startsWith("auth/"))
    }

    @Test
    fun `All main routes do not start with auth prefix`() {
        assertEquals(false, MainScreen.Catalog.route.startsWith("auth/"))
        assertEquals(false, MainScreen.Booking.route.startsWith("auth/"))
        assertEquals(false, MainScreen.Profile.route.startsWith("auth/"))
        assertEquals(false, MainScreen.Favorites.route.startsWith("auth/"))
        assertEquals(false, MainScreen.Reviews.route.startsWith("auth/"))
        assertEquals(false, MainScreen.Schedule.route.startsWith("auth/"))
    }

    // --- Edge cases ---

    @Test
    fun `Routes contain no spaces`() {
        val allScreens = listOf(
            AuthScreen.Login,
            AuthScreen.Register,
            AuthScreen.ForgotPassword,
            MainScreen.Catalog,
            MainScreen.Booking,
            MainScreen.Profile,
            MainScreen.Favorites,
            MainScreen.Reviews,
            MainScreen.Schedule,
        )

        for (screen in allScreens) {
            assertEquals(false, screen.route.contains(" "), "Route '${screen.route}' contains space")
        }
    }

    @Test
    fun `Routes contain no uppercase letters`() {
        val allScreens = listOf(
            AuthScreen.Login,
            AuthScreen.Register,
            MainScreen.Catalog,
            MainScreen.Booking,
            MainScreen.Profile,
            MainScreen.Favorites,
        )

        for (screen in allScreens) {
            assertEquals(
                false,
                screen.route.any { it.isUpperCase() },
                "Route '${screen.route}' contains uppercase letter",
            )
        }
    }
}