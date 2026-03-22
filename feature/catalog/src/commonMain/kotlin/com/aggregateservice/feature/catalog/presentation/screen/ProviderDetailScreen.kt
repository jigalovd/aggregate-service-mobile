package com.aggregateservice.feature.catalog.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.navigation.AuthPromptTrigger
import com.aggregateservice.core.navigation.AuthStateProvider
import com.aggregateservice.core.navigation.BookingNavigator
import com.aggregateservice.core.navigation.executeProtectedAction
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.Service
import com.aggregateservice.feature.catalog.presentation.model.ProviderDetailUiState
import com.aggregateservice.feature.catalog.presentation.screenmodel.ProviderDetailScreenModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Voyager Screen для деталей мастера.
 *
 * @property providerId ID мастера для загрузки
 */
data class ProviderDetailScreen(
    val providerId: String,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ProviderDetailScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        // Auth state for AuthGuard (via core:navigation abstraction)
        // Using koinInject for Compose-friendly DI without KoinComponent
        val authProvider: AuthStateProvider = koinInject()
        val isAuthenticated by authProvider.isAuthenticatedFlow.collectAsState()

        // Booking navigator for cross-feature navigation
        val bookingNavigator: BookingNavigator = koinInject()

        var showAuthPrompt by remember { mutableStateOf(false) }

        LaunchedEffect(providerId) {
            screenModel.initialize(providerId)
        }

        // Auth prompt dialog for guests
        if (showAuthPrompt) {
            AuthPromptDialog(
                onDismiss = { showAuthPrompt = false },
                onLogin = {
                    showAuthPrompt = false
                    // navigator.push(LoginScreen)
                },
                onRegister = {
                    showAuthPrompt = false
                    // navigator.push(RegisterScreen)
                },
            )
        }

        ProviderDetailScreenContent(
            uiState = uiState,
            onBackClick = { navigator.pop() },
            onFavoriteToggle = screenModel::onFavoriteToggle,
            onCategorySelected = screenModel::onCategorySelected,
            onServiceClick = { service ->
                // Service click could navigate to booking with pre-selected service
            },
            onBookClick = {
                executeProtectedAction(
                    isAuthenticated = isAuthenticated,
                    trigger = AuthPromptTrigger.Booking,
                    onShowPrompt = { showAuthPrompt = true },
                ) {
                    uiState.provider?.let { provider ->
                        navigator.push(
                            bookingNavigator.createSelectServiceScreen(
                                providerId = provider.id,
                                providerName = provider.businessName,
                            ),
                        )
                    }
                }
            },
            onRetry = screenModel::retry,
            onClearError = screenModel::clearError,
        )
    }
}

@Composable
private fun AuthPromptDialog(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign in to book") },
        text = { Text("Create an account or sign in to book appointments with our providers.") },
        confirmButton = {
            Button(onClick = onRegister) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onLogin) {
                Text("Sign In")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreenContent(
    uiState: ProviderDetailUiState,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onServiceClick: (Service) -> Unit,
    onBookClick: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(message = error.message ?: "Error")
                onClearError()
            }
        }
    }

    Scaffold(
        topBar = {
            ProviderDetailTopAppBar(
                isFavorite = uiState.isFavorite,
                onBackClick = onBackClick,
                onFavoriteToggle = onFavoriteToggle,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isLoaded) {
                BookButtonBar(onBookClick = onBookClick)
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> ProviderLoadingState()
            uiState.error != null && uiState.provider == null -> ProviderErrorState(
                error = uiState.error!!,
                onRetry = onRetry,
            )
            uiState.isLoaded && uiState.provider != null -> ProviderDetailContent(
                provider = uiState.provider,
                services = uiState.filteredServices,
                serviceCategories = uiState.serviceCategories,
                selectedCategoryId = uiState.selectedCategoryId,
                isLoadingServices = uiState.isLoadingServices,
                isOpenNow = uiState.isOpenNow,
                onCategorySelected = onCategorySelected,
                onServiceClick = { service -> onServiceClick(service) },
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailTopAppBar(
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }
        },
        actions = {
            IconButton(onClick = onFavoriteToggle) {
                Text(
                    text = if (isFavorite) "♥" else "♡",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        },
    )
}

@Composable
fun ProviderDetailContent(
    provider: Provider,
    services: List<Service>,
    serviceCategories: List<Pair<String, String>>,
    selectedCategoryId: String?,
    isLoadingServices: Boolean,
    isOpenNow: Boolean,
    onCategorySelected: (String?) -> Unit,
    onServiceClick: (Service) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            ProviderHeader(provider = provider, isOpenNow = isOpenNow)
        }

        if (serviceCategories.isNotEmpty()) {
            item {
                ServiceCategoryChips(
                    categories = serviceCategories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                )
            }
        }

        if (isLoadingServices && services.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (services.isNotEmpty()) {
            items(items = services, key = { it.id }) { service ->
                ServiceCard(service = service, onClick = { onServiceClick(service) })
            }
        } else {
            item {
                EmptyServicesState()
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProviderHeader(provider: Provider, isOpenNow: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = provider.businessName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = provider.formattedRating,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${provider.reviewCount} reviews)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (provider.isVerified) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✓ Verified",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📍", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = provider.location.city, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🕐", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOpenNow) "Open now" else "Closed",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOpenNow) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }

        provider.description?.let { desc ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceCategoryChips(
    categories: List<Pair<String, String>>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
            )
            categories.forEach { (id, name) ->
                FilterChip(
                    selected = selectedCategoryId == id,
                    onClick = { onCategorySelected(id) },
                    label = { Text(name) },
                )
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = service.name, style = MaterialTheme.typography.titleSmall)
                    service.description?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = service.formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${service.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun BookButtonBar(onBookClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Button(onClick = onBookClick, modifier = Modifier.fillMaxWidth()) {
            Text("Book Now")
        }
    }
}

@Composable
fun ProviderLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ProviderErrorState(
    error: com.aggregateservice.core.network.AppError,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Error loading", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error.message ?: "Unknown error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyServicesState() {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "No services available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
