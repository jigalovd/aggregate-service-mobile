package com.aggregateservice.app.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

/**
 * Bottom navigation host composable that wraps the main content with a bottom navigation bar.
 *
 * @param startScreen The initial screen to display
 * @param modifier Optional modifier for the container
 */
@Composable
fun AppBottomNavHost(
    startScreen: Screen,
    modifier: Modifier = Modifier,
) {
    Navigator(screen = startScreen) { navigator ->
        val bottomNavItems = listOf(
            BottomNavItem.Catalog,
            BottomNavItem.Search,
            BottomNavItem.Favorites,
            BottomNavItem.Profile,
        )

        // Track selected tab index - default to Catalog (index 0)
        var selectedIndex by remember { mutableIntStateOf(0) }

        // Update selected index when current screen changes
        val currentScreen = navigator.lastItem
        selectedIndex = bottomNavItems.indexOfFirst { item ->
            // Compare by screen instance type name
            item.screen::class.simpleName == currentScreen::class.simpleName
        }.coerceAtLeast(0)

        // Render the current screen content FIRST
        currentScreen.Content()

        // Then render the bottom navigation bar as an overlay
        NavigationBar {
            bottomNavItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = { Text(item.icon) },
                    label = { Text(item.title) },
                    selected = selectedIndex == index,
                    onClick = {
                        if (selectedIndex != index) {
                            selectedIndex = index
                            navigator.replace(item.screen)
                        }
                    },
                )
            }
        }
    }
}
