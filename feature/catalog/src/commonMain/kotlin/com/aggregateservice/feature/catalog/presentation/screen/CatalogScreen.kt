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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.presentation.model.CatalogUiState
import com.aggregateservice.feature.catalog.presentation.screenmodel.CatalogScreenModel
import kotlinx.coroutines.launch

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
        val screenModel = koinScreenModel<CatalogScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        CatalogScreenContent(
            uiState = uiState,
            onSearchQueryChanged = screenModel::onSearchQueryChanged,
            onSearchSubmit = screenModel::onSearchSubmit,
            onCategorySelected = screenModel::onCategorySelected,
            onClearFilters = screenModel::onClearFilters,
            onLoadMore = screenModel::loadMore,
            onProviderClick = { provider ->
                // TODO: Navigate to provider details
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

    // Show error message in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error.message ?: "Произошла ошибка",
                )
                onClearError()
            }
        }
    }

    // Load more when scrolled to end
    LaunchedEffect(listState) {
        val layoutInfo = listState.layoutInfo
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val totalItems = layoutInfo.totalItemsCount

        if (lastVisibleItem != null && lastVisibleItem >= totalItems - 5) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            CatalogTopAppBar(
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
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content
            when {
                uiState.isLoading && uiState.providers.isEmpty() -> {
                    // Full screen loading
                    LoadingState()
                }
                uiState.isEmpty() -> {
                    // Empty state
                    EmptyState(
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
    filtersCount: Int,
    onFilterClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("Каталог мастеров") },
        actions = {
            Row {
                IconButton(onClick = onFilterClick) {
                    Text("🔍${if (filtersCount > 0) " ($filtersCount)" else ""}")
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
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("Все") },
        )
        categories.forEach { category ->
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
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка...",
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
                text = "Мастера не найдены",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hasFilters) {
                Text(
                    text = "Попробуйте изменить фильтры",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onClearFilters) {
                    Text("Сбросить фильтры")
                }
            } else {
                Text(
                    text = "Попробуйте изменить параметры поиска",
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
            )
        }

        // Loading more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/**
 * Card for displaying provider info.
 *
 * TODO: Move to separate file when design system is finalized
 */
@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = provider.businessName,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            provider.shortDescription?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "⭐ ${provider.formattedRating}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = " • ${provider.reviewCount} отзывов",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = provider.location.city,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
