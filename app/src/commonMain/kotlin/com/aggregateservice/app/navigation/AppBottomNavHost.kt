package com.aggregateservice.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.InitializeAuthUseCase
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.feature.catalog.presentation.screenmodel.SearchScreenModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Bottom navigation host composable that wraps the main content with a bottom navigation bar.
 * Uses Scaffold to properly position the bottom bar at the bottom of the screen.
 *
 * Search functionality is implemented as a modal bottom sheet (per UI-02 requirement).
 * Back gesture dismisses the sheet via BackHandler.
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
    val authState by authStateProvider.authState.collectAsState()
    val isAuthenticated = authState is AuthState.Authenticated
    val currentUserId = (authState as? AuthState.Authenticated)?.userId
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

    // Search bottom sheet state
    var showSearchSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Navigator(screen = startScreen) { navigator ->
        val bottomNavItems =
            listOf(
                BottomNavItem.Catalog,
                BottomNavItem.Bookings,
                BottomNavItem.Favorites,
                BottomNavItem.Profile,
            )

        // Track selected tab index - default to Catalog (index 0)
        var selectedIndex by remember { mutableIntStateOf(0) }

        // Update selected index when current screen changes
        val currentScreen = navigator.lastItem
        selectedIndex =
            bottomNavItems
                .indexOfFirst { item ->
                    // Compare by screen instance type name
                    item.screen::class.simpleName == currentScreen::class.simpleName
                }.coerceAtLeast(0)

        Scaffold(
            // No topBar - removed per UI-01 requirement (no "Aggregate Service" title)
            bottomBar = {
                // Bottom navigation - 4 items per UI-03
                NavigationBar(
                    containerColor =
                        androidx.compose.ui.graphics
                            .Color(0xFFADD8E6),
                ) {
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
            // Content with search button overlaid at bottom
            Box(modifier = Modifier.fillMaxSize()) {
                // Apply Scaffold paddingValues so child screens account for NavigationBar height
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    currentScreen.Content()
                }

                // Search FAB — only on tab screens, hidden on detail/pushed screens
                val isTabScreen =
                    bottomNavItems.any { it.screen::class.simpleName == currentScreen::class.simpleName }

                if (isTabScreen) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    end = 16.dp,
                                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                                )
                                .size(48.dp)
                                .background(
                                    androidx.compose.ui.graphics
                                        .Color(0xFFADD8E6),
                                    CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick = { showSearchSheet = true },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint =
                                    androidx.compose.ui.graphics
                                        .Color(0xFF333333),
                            )
                        }
                    }
                }
            }
        }

        // Search Modal Bottom Sheet
        if (showSearchSheet) {
            // Back gesture handling for bottom sheet dismiss
            BackHandler(enabled = showSearchSheet) {
                showSearchSheet = false
            }
            SearchBottomSheet(
                onDismiss = { showSearchSheet = false },
                sheetState = sheetState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBottomSheet(
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
) {
    // Create a local screen model for search
    val screenModel: SearchScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Header with close button - NO TopAppBar (per UI-02 requirement)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search text field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { screenModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search providers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { screenModel.onSearchSubmit() }),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search content based on state
            when {
                uiState.isLoading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.searchQuery.isBlank() -> {
                    // Show hint when no query
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Enter a search query",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                uiState.providers.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    Text(
                        text = "${uiState.providers.size} results",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
