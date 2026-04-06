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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.reviews.domain.model.Review
import com.aggregateservice.feature.reviews.domain.model.ReviewStats
import com.aggregateservice.feature.reviews.presentation.component.ReviewCard
import com.aggregateservice.feature.reviews.presentation.component.WriteReviewDialog
import com.aggregateservice.feature.reviews.presentation.model.ReviewsUiState
import com.aggregateservice.feature.reviews.presentation.model.WriteReviewUiState
import com.aggregateservice.feature.reviews.presentation.screenmodel.ReviewsScreenModel
import com.aggregateservice.feature.reviews.presentation.screenmodel.WriteReviewScreenModel
import org.koin.compose.koinInject

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
        val writeReviewScreenModel: WriteReviewScreenModel = koinScreenModel()
        val writeReviewState by writeReviewScreenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val i18nProvider: I18nProvider = koinInject()

        var showWriteReviewDialog by remember { mutableStateOf(false) }

        LaunchedEffect(providerId) {
            screenModel.initialize(providerId)
        }

        // Initialize write review dialog when shown
        LaunchedEffect(showWriteReviewDialog) {
            if (showWriteReviewDialog && writeReviewState.bookingId.isEmpty()) {
                // TODO: Get actual bookingId for this provider (requires booking flow integration)
                writeReviewScreenModel.initialize(
                    bookingId = "placeholder-booking-id",
                    providerName = providerName,
                )
            }
        }

        ReviewsScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            providerName = providerName,
            onRefresh = { screenModel.refresh() },
            onLoadMore = { screenModel.loadMore() },
            onBackClick = { navigator.pop() },
            onWriteReviewClick = { showWriteReviewDialog = true },
        )

        // Write Review Dialog
        if (showWriteReviewDialog) {
            WriteReviewDialog(
                state = writeReviewState,
                onRatingChange = { writeReviewScreenModel.setRating(it) },
                onCommentChange = { writeReviewScreenModel.setComment(it) },
                onSubmit = { writeReviewScreenModel.submitReview() },
                onDismiss = {
                    showWriteReviewDialog = false
                    // Refresh reviews after submission
                    if (writeReviewState.isSuccess) {
                        screenModel.refresh()
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewsScreenContent(
    i18nProvider: I18nProvider,
    uiState: ReviewsUiState,
    providerName: String,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onBackClick: () -> Unit,
    onWriteReviewClick: () -> Unit,
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
                        message = uiState.error?.message ?: i18nProvider[StringKey.Error.UNKNOWN],
                        onRetry = onRefresh,
                        i18nProvider = i18nProvider,
                    )
                }

                uiState.isEmpty -> {
                    EmptyState(
                        i18nProvider = i18nProvider,
                        onWriteReviewClick = onWriteReviewClick,
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                    ) {
                        // Stats header
                        uiState.stats?.let { stats ->
                            item {
                                ReviewStatsHeader(stats = stats, i18nProvider = i18nProvider)
                            }
                        }

                        // Reviews list
                        items(
                            items = uiState.reviews,
                            key = { it.id },
                        ) { review ->
                            ReviewCard(
                                review = review,
                                modifier = Modifier.padding(horizontal = Spacing.MD),
                            )
                        }

                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.MD),
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
    i18nProvider: I18nProvider,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.MD),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.MD),
    ) {
        // Average rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            Text(
                text = stats.formattedAverageRating,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.XXS)) {
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
                    text = i18nProvider[StringKey.Plurals.REVIEWS_COUNT]
                        .replace("{count}", "${stats.totalReviews}"),
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
    percentage: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
    ) {
        Text(
            text = "$rating",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(Spacing.MD),
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
                .height(Spacing.SM)
                .fillMaxWidth((percentage / 100).toFloat()),
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
            modifier = Modifier.width(Spacing.XXS),
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
    i18nProvider: I18nProvider,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.MD),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) {
                Text(i18nProvider[StringKey.Reviews.RETRY])
            }
        }
    }
}

@Composable
private fun EmptyState(
    i18nProvider: I18nProvider,
    onWriteReviewClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            Text(
                text = "💬",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = i18nProvider[StringKey.Reviews.NO_REVIEWS],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            TextButton(onClick = onWriteReviewClick) {
                Text(i18nProvider[StringKey.Reviews.WRITE_REVIEW])
            }
        }
    }
}
