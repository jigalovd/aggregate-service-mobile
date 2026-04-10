package com.aggregateservice.app.navigation

import cafe.adriel.voyager.core.screen.Screen
import com.aggregateservice.feature.booking.presentation.screen.BookingHistoryScreen
import com.aggregateservice.feature.catalog.presentation.screen.CatalogScreen
import com.aggregateservice.feature.favorites.presentation.screen.FavoritesScreen
import com.aggregateservice.feature.profile.presentation.screen.ProfileScreen

/**
 * Sealed class representing bottom navigation items.
 *
 * Each item has a title, icon identifier, and associated Screen.
 * Note: navigator.replace() destroys ScreenModels on tab switch.
 * Repository-level caching compensates for ScreenModel recreation.
 */
sealed class BottomNavItem(
    val title: String,
    val icon: String,
    val screen: Screen,
) {
    data object Catalog : BottomNavItem(
        title = "Catalog",
        icon = "home",
        screen = CatalogScreen,
    )

    // Search removed from bottom nav per UI-03 (becomes modal bottom sheet trigger)
    // TODO: Search icon in nav bar will trigger showSearchSheet instead of navigating

    data object Bookings : BottomNavItem(
        title = "Bookings",
        icon = "calendar",
        screen = BookingHistoryScreen,
    )

    data object Favorites : BottomNavItem(
        title = "Favorites",
        icon = "heart",
        screen = FavoritesScreen,
    )

    data object Profile : BottomNavItem(
        title = "Profile",
        icon = "person",
        screen = ProfileScreen,
    )
}
