package com.aggregateservice.feature.catalog.presentation.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.presentation.component.ProviderCard
import com.aggregateservice.feature.catalog.presentation.model.CatalogUiState
import com.aggregateservice.feature.catalog.presentation.screenmodel.CatalogScreenModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Composable Screen для каталога мастеров (Presentation слой).
 *
 * **Architecture:**
 * - Screen (Voyager) = Screen в navigation graph
 * - Использует ScreenModel для управления состоянием
 * - Отображает State через Compose
 *
 * **UDF Pattern:**
 * ```
 * UI Events (onClick, onScroll)
 *   ↓
 * ScreenModel (обработка)
 *   ↓
 * UseCase (бизнес-логика)
 *   ↓
 * StateFlow (новый state)
 *   ↓
 * UI рекомпозиция
 * ```
 */
class CatalogScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<CatalogScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        CatalogScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onSearchQueryChanged = screenModel::onSearchQueryChanged,
            onSearchSubmit = screenModel::onSearchSubmit,
            onCategorySelected = screenModel::onCategorySelected,
            onClearFilters = screenModel::onClearFilters,
            onLoadMore = screenModel::loadMore,
            onProviderClick = { provider ->
                navigator.push(ProviderDetailScreen(providerId = provider.id))
            },
            onClearError = screenModel::clearError,
        )
    }
}

/**
 * Content composable для CatalogScreen.
 *
 * **Stateless:** Не хранит состояние, только отображает переданный state.
 * **Testable:** Можно тестировать отдельно от Screen, передавая тестовый state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreenContent(
    i18nProvider: I18nProvider,
    uiState: CatalogUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onProviderClick: (Provider) -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Derived state for pagination - prevents redundant onLoadMore calls
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem >= totalItems - 5
        }
    }

    // Show error message in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: i18nProvider[StringKey.Error.UNKNOWN],
                )
                onClearError()
            }
        }
    }

    // Load more when scrolled to end
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            CatalogTopAppBar(
                i18nProvider = i18nProvider,
                filtersCount = uiState.activeFiltersCount,
                onFilterClick = { /* TODO: Open filters bottom sheet */ },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Categories horizontal scroll
            if (uiState.categories.isNotEmpty()) {
                CategoryChipsRow(
                    i18nProvider = i18nProvider,
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
                Spacer(modifier = Modifier.height(Spacing.SM))
            }

            // Content
            when {
                uiState.isLoading && uiState.providers.isEmpty() -> {
                    // Full screen loading
                    LoadingState(i18nProvider = i18nProvider)
                }
                uiState.isEmpty() -> {
                    // Empty state
                    EmptyState(
                        i18nProvider = i18nProvider,
                        hasFilters = uiState.hasActiveFilters(),
                        onClearFilters = onClearFilters,
                    )
                }
                else -> {
                    // Providers list
                    ProvidersList(
                        providers = uiState.providers,
                        isLoadingMore = uiState.isLoadingMore,
                        listState = listState,
                        onProviderClick = onProviderClick,
                        i18nProvider = i18nProvider,
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar для каталога.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopAppBar(
    i18nProvider: I18nProvider,
    filtersCount: Int,
    onFilterClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(i18nProvider[StringKey.Catalog.TITLE]) },
        actions = {
            Row {
                IconButton(onClick = onFilterClick) {
                    Text("${i18nProvider[StringKey.SEARCH]}${if (filtersCount > 0) " ($filtersCount)" else ""}")
                }
            }
        },
    )
}

/**
 * Horizontal scrollable row of category chips.
 */
@Composable
fun CategoryChipsRow(
    i18nProvider: I18nProvider,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.MD),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(i18nProvider[StringKey.Catalog.ALL]) },
            )
        }
        items(
            items = categories,
            key = { it.id },
        ) { category ->
            FilterChip(
                selected = selectedCategory?.id == category.id,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
            )
        }
    }
}

/**
 * Loading state component.
 */
@Composable
fun LoadingState(i18nProvider: I18nProvider) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(Spacing.MD))
            Text(
                text = i18nProvider[StringKey.LOADING],
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Empty state component.
 */
@Composable
fun EmptyState(
    i18nProvider: I18nProvider,
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = i18nProvider[StringKey.Catalog.NO_RESULTS],
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(Spacing.SM))
            if (hasFilters) {
                Text(
                    text = i18nProvider[StringKey.Catalog.FILTER_BY_CATEGORY],
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(Spacing.MD))
                Button(onClick = onClearFilters) {
                    Text(i18nProvider[StringKey.CLEAR])
                }
            } else {
                Text(
                    text = i18nProvider[StringKey.SEARCH],
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/**
 * List of providers.
 */
@Composable
fun ProvidersList(
    providers: List<Provider>,
    isLoadingMore: Boolean,
    listState: LazyListState,
    onProviderClick: (Provider) -> Unit,
    i18nProvider: I18nProvider,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = providers,
            key = { it.id },
        ) { provider ->
            ProviderCard(
                provider = provider,
                onClick = { onProviderClick(provider) },
                i18nProvider = i18nProvider,
            )
        }

        // Loading more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.MD),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Spacing.SM),
                    )
                }
            }
        }
    }
}
