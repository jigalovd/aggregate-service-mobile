package com.aggregateservice.app.navigation

import cafe.adriel.voyager.core.screen.Screen
import com.aggregateservice.feature.catalog.presentation.screen.CatalogScreen
import com.aggregateservice.feature.catalog.presentation.screen.SearchScreen
import com.aggregateservice.feature.favorites.presentation.screen.FavoritesScreen
import com.aggregateservice.feature.profile.presentation.screen.ProfileScreen

/**
 * Sealed class representing bottom navigation items.
 *
 * Each item has a title, icon identifier, and associated Screen.
 * Screen instances are cached to ensure ScreenModel state persists across tab switches.
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

    data object Search : BottomNavItem(
        title = "Search",
        icon = "search",
        screen = SearchScreen,
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
