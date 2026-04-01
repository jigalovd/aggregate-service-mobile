package com.aggregateservice.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.navigation.AuthStateProvider
import org.koin.compose.koinInject

/**
 * Bottom navigation host composable that wraps the main content with a bottom navigation bar.
 * Uses Scaffold to properly position the bottom bar at the bottom of the screen.
 *
 * @param startScreen The initial screen to display
 * @param modifier Optional modifier for the container
 */
@Composable
fun AppBottomNavHost(
    startScreen: Screen,
    modifier: Modifier = Modifier,
) {
    val authStateProvider: AuthStateProvider = koinInject()
    val isAuthenticated by authStateProvider.isAuthenticatedFlow.collectAsState(initial = false)
    val currentUserId by authStateProvider.currentUserIdFlow.collectAsState(initial = null)

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

        Scaffold(
            bottomBar = {
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
                    // Auth state indicator - show user avatar when authenticated
                    if (isAuthenticated) {
                        NavigationBarItem(
                            icon = { Text("👤") },
                            label = { Text(currentUserId?.take(8) ?: "User") },
                            selected = false,
                            onClick = { },
                            enabled = false,
                        )
                    }
                }
            },
        ) { paddingValues ->
            // Content fills the space above the bottom navigation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                currentScreen.Content()
            }
        }
    }
}
