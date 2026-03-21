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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.feature.catalog.domain.model.Category
import com.aggregateservice.feature.catalog.domain.model.Provider
import com.aggregateservice.feature.catalog.domain.model.SearchFilters
import com.aggregateservice.feature.catalog.presentation.model.SearchUiState
import com.aggregateservice.feature.catalog.presentation.screenmodel.SearchScreenModel
import kotlinx.coroutines.launch

/**
 * Voyager Screen для поиска мастеров.
 */
object SearchScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SearchScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        SearchScreenContent(
            uiState = uiState,
            onSearchQueryChanged = screenModel::onSearchQueryChanged,
            onSearchSubmit = screenModel::onSearchSubmit,
            onCategoryToggle = screenModel::onCategoryToggle,
            onFilterToggle = screenModel::onFilterSheetToggle,
            onClearFilters = screenModel::onClearFilters,
            onLoadMore = screenModel::loadMore,
            onProviderClick = { provider ->
                navigator.push(ProviderDetailScreen(providerId = provider.id))
            },
            onRecentSearchClick = screenModel::onRecentSearchClick,
            onClearError = screenModel::clearError,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    uiState: SearchUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onCategoryToggle: (String) -> Unit,
    onFilterToggle: () -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onProviderClick: (Provider) -> Unit,
    onRecentSearchClick: (String) -> Unit,
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

    // Show error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(message = error.message ?: "Error")
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
            SearchTopAppBar(
                filtersCount = uiState.activeFiltersCount,
                onFilterClick = onFilterToggle,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Search field
            SearchField(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                onSubmit = onSearchSubmit,
            )

            // Category chips
            if (uiState.categories.isNotEmpty()) {
                CategoryFilterChips(
                    categories = uiState.categories,
                    selectedCategories = uiState.selectedCategories,
                    onCategoryToggle = onCategoryToggle,
                )
            }

            // Content
            when {
                uiState.isLoading && uiState.providers.isEmpty() -> {
                    SearchLoadingState()
                }
                uiState.searchQuery.isBlank() -> {
                    RecentSearchesSection(
                        recentSearches = uiState.recentSearches,
                        onRecentSearchClick = onRecentSearchClick,
                    )
                }
                !uiState.hasResults && !uiState.isLoading -> {
                    SearchEmptyState(
                        hasFilters = uiState.hasActiveFilters,
                        onClearFilters = onClearFilters,
                    )
                }
                else -> {
                    SearchResultsList(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    filtersCount: Int,
    onFilterClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("Поиск") },
        actions = {
            IconButton(onClick = onFilterClick) {
                Text("🔍${if (filtersCount > 0) " ($filtersCount)" else ""}")
            }
        },
    )
}

@Composable
fun SearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Поиск мастеров, услуг...") },
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Text("✕")
                }
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterChips(
    categories: List<Category>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category.id in selectedCategories,
                onClick = { onCategoryToggle(category.id) },
                label = { Text(category.name) },
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SearchLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SearchEmptyState(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ничего не найдено",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hasFilters) {
                Text(
                    text = "Попробуйте изменить фильтры",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onClearFilters) {
                    Text("Сбросить фильтры")
                }
            }
        }
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
) {
    if (recentSearches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔍",
                    style = MaterialTheme.typography.displayMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Найдите мастера",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Введите название услуги или имя мастера",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Недавние поиски",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            recentSearches.forEach { query ->
                androidx.compose.material3.TextButton(
                    onClick = { onRecentSearchClick(query) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsList(
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

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
