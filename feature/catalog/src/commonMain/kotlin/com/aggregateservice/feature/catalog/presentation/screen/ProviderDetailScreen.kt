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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.aggregateservice.core.auth.contract.AuthNavigator
import com.aggregateservice.core.auth.contract.AuthPromptTrigger
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.state.AuthState
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.navigation.BookingNavigator
import com.aggregateservice.core.navigation.executeProtectedAction
import com.aggregateservice.core.theme.Spacing
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

        // Auth state for AuthGuard (via core:auth-api contracts)
        val authProvider: AuthStateProvider = koinInject()
        val authState by authProvider.authState.collectAsState()
        val isAuthenticated = authState is AuthState.Authenticated

        // Booking navigator for cross-feature navigation
        val bookingNavigator: BookingNavigator = koinInject()

        // Auth navigator for login screen (replaces AuthPromptDialog)
        val authNavigator: AuthNavigator = koinInject()

        // i18n provider for localized strings
        val i18nProvider: I18nProvider = koinInject()

        LaunchedEffect(providerId) {
            screenModel.initialize(providerId)
        }

        ProviderDetailScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onBackClick = { navigator.pop() },
            onFavoriteToggle = {
                executeProtectedAction(
                    isAuthenticated = isAuthenticated,
                    trigger = AuthPromptTrigger.FAVORITES,
                    onShowPrompt = { _ ->
                        navigator.push(authNavigator.createLoginScreen())
                    },
                    action = screenModel::onFavoriteToggle,
                )
            },
            onCategorySelected = screenModel::onCategorySelected,
            onServiceToggle = screenModel::onServiceToggle,
            onBookSelected = {
                executeProtectedAction(
                    isAuthenticated = isAuthenticated,
                    trigger = AuthPromptTrigger.BOOKING,
                    onShowPrompt = { _ ->
                        navigator.push(authNavigator.createLoginScreen())
                    },
                ) {
                    uiState.provider?.let { provider ->
                        navigator.push(
                            bookingNavigator.createSelectDateTimeScreen(
                                providerId = provider.id,
                                providerName = provider.businessName,
                                serviceIds = uiState.selectedServiceIds.toList(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreenContent(
    i18nProvider: I18nProvider,
    uiState: ProviderDetailUiState,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onServiceToggle: (String) -> Unit,
    onBookSelected: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(message = error.message ?: i18nProvider[StringKey.ERROR])
                onClearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isLoaded && uiState.selectedServiceIds.isNotEmpty()) {
                SelectedServicesBar(
                    selectedCount = uiState.selectedServiceIds.size,
                    totalPrice = uiState.totalPrice,
                    totalDurationMinutes = uiState.totalDurationMinutes,
                    currency =
                        uiState.selectedServices
                            .firstOrNull()
                            ?.price
                            ?.currency ?: "ILS",
                    onBookClick = onBookSelected,
                    i18nProvider = i18nProvider,
                )
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> ProviderLoadingState()
            uiState.error != null && uiState.provider == null ->
                ProviderErrorState(
                    error = uiState.error!!,
                    onRetry = onRetry,
                )
            uiState.isLoaded && uiState.provider != null ->
                ProviderDetailContent(
                    provider = uiState.provider,
                    services = uiState.filteredServices,
                    selectedServiceIds = uiState.selectedServiceIds,
                    serviceCategories = uiState.serviceCategories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    isLoadingServices = uiState.isLoadingServices,
                    isOpenNow = uiState.isOpenNow,
                    onCategorySelected = onCategorySelected,
                    onServiceToggle = onServiceToggle,
                    i18nProvider = i18nProvider,
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
                    color =
                        if (isFavorite) {
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
    selectedServiceIds: Set<String>,
    serviceCategories: List<Pair<String, String>>,
    selectedCategoryId: String?,
    isLoadingServices: Boolean,
    isOpenNow: Boolean,
    onCategorySelected: (String?) -> Unit,
    onServiceToggle: (String) -> Unit,
    i18nProvider: I18nProvider,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            ProviderHeader(provider = provider, isOpenNow = isOpenNow, i18nProvider = i18nProvider)
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
                    modifier = Modifier.fillMaxWidth().padding(Spacing.XL),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (services.isNotEmpty()) {
            items(items = services, key = { it.id }) { service ->
                ServiceCard(
                    service = service,
                    isSelected = service.id in selectedServiceIds,
                    onToggle = { onServiceToggle(service.id) },
                )
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
fun ProviderHeader(
    provider: Provider,
    isOpenNow: Boolean,
    i18nProvider: I18nProvider,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.MD)) {
        Text(
            text = provider.businessName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(Spacing.SM))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(Spacing.XS))
            Text(
                text = provider.formattedRating,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(Spacing.SM))
            Text(
                text = "(${i18nProvider.get(StringKey.Plurals.REVIEWS_COUNT, provider.reviewCount)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (provider.isVerified) {
            Spacer(modifier = Modifier.height(Spacing.SM))
            Text(
                text = "✓ ${i18nProvider[StringKey.Provider.VERIFIED]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.MD))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📍", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(Spacing.SM))
            Text(text = provider.location.city, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(Spacing.SM))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🕐", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(Spacing.SM))
            Text(
                text = if (isOpenNow) i18nProvider[StringKey.Provider.OPEN_NOW] else i18nProvider[StringKey.Provider.CLOSED],
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (isOpenNow) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }

        provider.description?.let { desc ->
            Spacer(modifier = Modifier.height(Spacing.MD))
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
    val i18nProvider: I18nProvider = koinInject()
    Column(modifier = Modifier.padding(horizontal = Spacing.MD)) {
        Text(
            text = i18nProvider[StringKey.Provider.SERVICES],
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(Spacing.SM))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text(i18nProvider[StringKey.Catalog.ALL]) },
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
fun ServiceCard(service: Service, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.MD, vertical = Spacing.XS),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.MD),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
            )
            Spacer(modifier = Modifier.width(Spacing.SM))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = service.name, style = MaterialTheme.typography.titleSmall)
                service.description?.let { desc ->
                    Spacer(modifier = Modifier.height(Spacing.XS))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.SM))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${service.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = service.formattedPrice,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedServicesBar(
    selectedCount: Int,
    totalPrice: Double,
    totalDurationMinutes: Int,
    currency: String,
    onBookClick: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.MD)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = i18nProvider.get(StringKey.Plurals.SERVICES_COUNT, selectedCount),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "%.0f %s".format(totalPrice, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = i18nProvider.get(StringKey.Plurals.MINUTES, totalDurationMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.SM))
            Button(onClick = onBookClick, modifier = Modifier.fillMaxWidth()) {
                Text(i18nProvider[StringKey.Booking.BOOK_SELECTED])
            }
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
    val i18nProvider: I18nProvider = koinInject()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = i18nProvider[StringKey.ERROR], style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(Spacing.SM))
            Text(
                text = error.message ?: i18nProvider[StringKey.Error.UNKNOWN],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.MD))
            OutlinedButton(onClick = onRetry) {
                Text(i18nProvider[StringKey.RETRY])
            }
        }
    }
}

@Composable
fun EmptyServicesState() {
    val i18nProvider: I18nProvider = koinInject()
    Box(modifier = Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
        Text(
            text = i18nProvider[StringKey.Scheduling.NO_SLOTS_AVAILABLE],
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
