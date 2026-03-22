package com.aggregateservice.feature.reviews.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats
import com.aggregateservice.feature.reviews.presentation.component.ReviewCard
import com.aggregateservice.feature.reviews.presentation.model.ReviewsUiState
import com.aggregateservice.feature.reviews.presentation.screenmodel.ReviewsScreenModel

/**
 * Screen for displaying reviews of a provider.
 *
 * @property providerId The ID of the provider to show reviews for
 * @property providerName The name of the provider (for display)
 */
class ReviewsScreen(
    private val providerId: String,
    private val providerName: String = "",
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ReviewsScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(providerId) {
            screenModel.initialize(providerId)
        }

        ReviewsScreenContent(
            uiState = uiState,
            providerName = providerName,
            onRefresh = { screenModel.refresh() },
            onLoadMore = { screenModel.loadMore() },
            onBackClick = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewsScreenContent(
    uiState: ReviewsUiState,
    providerName: String,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onBackClick: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    // Infinity scroll detection
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem >= totalItems - 3 && uiState.canLoadMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (providerName.isNotBlank()) "Отзывы: $providerName" else "Отзывы",
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Назад")
                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState.isLoading && uiState.reviews.isEmpty() -> {
                    LoadingState()
                }

                uiState.error != null && uiState.reviews.isEmpty() -> {
                    ErrorState(
                        message = uiState.error?.message ?: "Произошла ошибка",
                        onRetry = onRefresh,
                    )
                }

                uiState.isEmpty -> {
                    EmptyState()
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Stats header
                        uiState.stats?.let { stats ->
                            item {
                                ReviewStatsHeader(stats = stats)
                            }
                        }

                        // Reviews list
                        items(
                            items = uiState.reviews,
                            key = { it.id },
                        ) { review ->
                            ReviewCard(
                                review = review,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }

                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStatsHeader(
    stats: ReviewStats,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Average rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stats.formattedAverageRating,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { index ->
                        val isFilled = index < stats.averageRating.toInt()
                        Text(
                            text = "★",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isFilled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }
                }
                Text(
                    text = "${stats.totalReviews} отзывов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Rating distribution
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (rating in 5 downTo 1) {
                RatingDistributionRow(
                    rating = rating,
                    count = stats.getCountForRating(rating),
                    percentage = stats.getPercentageForRating(rating),
                )
            }
        }
    }
}

@Composable
private fun RatingDistributionRow(
    rating: Int,
    count: Int,
    percentage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$rating",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(16.dp),
        )
        Text(
            text = "★",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        // Progress bar simulation
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .fillMaxWidth(percentage / 100f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "💬",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "Пока нет отзывов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "Будьте первым, кто оставит отзыв!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
