package com.aggregateservice.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.navigation.AuthStateProvider
import com.aggregateservice.feature.auth.domain.usecase.InitializeAuthUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Bottom navigation host composable that wraps the main content with a bottom navigation bar.
 * Uses Scaffold to properly position the bottom bar at the bottom of the screen.
 *
 * @param startScreen The initial screen to display
 * @param modifier Optional modifier for the container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomNavHost(
    startScreen: Screen,
    modifier: Modifier = Modifier,
) {
    val authStateProvider: AuthStateProvider = koinInject()
    val initializeAuthUseCase: InitializeAuthUseCase = koinInject()
    val isAuthenticated by authStateProvider.isAuthenticatedFlow.collectAsState(initial = false)
    val currentUserId by authStateProvider.currentUserIdFlow.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    // Initialize auth state on first composition (silent re-login)
    var authInitialized by remember { mutableIntStateOf(0) }
    LaunchedEffect(authInitialized) {
        if (authInitialized == 0) {
            authInitialized = 1
            coroutineScope.launch {
                initializeAuthUseCase()
            }
        }
    }

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
            topBar = {
                TopAppBar(
                    title = { Text("Aggregate Service") },
                    actions = {
                        // Auth state indicator - show user avatar when authenticated
                        if (isAuthenticated) {
                            Row(
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = currentUserId?.take(8) ?: "User",
                                    modifier = Modifier.padding(top = 12.dp, end = 4.dp),
                                )
                                Text(text = "👤")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(),
                )
            },
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
