package com.aggregateservice.feature.provider.dashboard

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.provider.dashboard.domain.model.BookingStatus
import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats
import com.aggregateservice.feature.provider.dashboard.presentation.model.ProviderDashboardUiState
import com.aggregateservice.feature.provider.dashboard.presentation.screenmodel.ProviderDashboardScreenModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Provider Dashboard Voyager Screen.
 *
 * Displays provider-specific overview: today's bookings, earnings summary, and provider stats.
 * Requires PROVIDER role to access.
 */
object ProviderDashboardScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProviderDashboardScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        ProviderDashboardScreenContent(
            uiState = uiState,
            onRefresh = { screenModel.refresh() },
            onRetry = { screenModel.retry() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreenContent(
    uiState: ProviderDashboardUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Dashboard") },
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is ProviderDashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProviderDashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error.message ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.MD))
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ProviderDashboardUiState.Content -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    if (uiState.isEmpty) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No bookings today",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.MD),
                        ) {
                            // Earnings Summary Card
                            item {
                                EarningsSummaryCard(
                                    earningsSummary = uiState.earningsSummary,
                                    modifier = Modifier.padding(vertical = Spacing.SM),
                                )
                            }

                            // Provider Stats Card
                            item {
                                ProviderStatsCard(
                                    providerStats = uiState.providerStats,
                                    modifier = Modifier.padding(vertical = Spacing.SM),
                                )
                            }

                            // Today's Bookings Header
                            item {
                                Text(
                                    text = "Today's Bookings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = Spacing.SM),
                                )
                            }

                            // Booking Items
                            items(uiState.todaysBookings, key = { it.id }) { booking ->
                                DashboardBookingCard(
                                    booking = booking,
                                    modifier = Modifier.padding(vertical = Spacing.SM),
                                )
                            }

                            // Bottom spacing
                            item {
                                Spacer(modifier = Modifier.height(Spacing.LG))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EarningsSummaryCard(
    earningsSummary: EarningsSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.MD),
        ) {
            Text(
                text = "Earnings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                EarningsStatItem(
                    label = "Today",
                    value = earningsSummary.formattedToday,
                )
                EarningsStatItem(
                    label = "This Week",
                    value = earningsSummary.formattedWeek,
                )
                EarningsStatItem(
                    label = "This Month",
                    value = earningsSummary.formattedMonth,
                )
            }
        }
    }
}

@Composable
private fun EarningsStatItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun ProviderStatsCard(
    providerStats: ProviderStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.MD),
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "Pending",
                    value = providerStats.pendingRequests.toString(),
                )
                StatItem(
                    label = "Active",
                    value = providerStats.activeBookings.toString(),
                )
                StatItem(
                    label = "Completed",
                    value = providerStats.completedToday.toString(),
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun DashboardBookingCard(
    booking: DashboardBooking,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.MD),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = booking.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                BookingStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(Spacing.SM))

            Text(
                text = booking.serviceName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.SM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatTime(booking.startTime) + " - " + formatTime(booking.endTime),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = booking.formattedDuration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.XS))

            Text(
                text = booking.formattedPrice,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun BookingStatusChip(status: BookingStatus) {
    val (text, color) =
        when (status) {
            BookingStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
            BookingStatus.CONFIRMED -> "Confirmed" to MaterialTheme.colorScheme.primary
            BookingStatus.IN_PROGRESS -> "In Progress" to MaterialTheme.colorScheme.secondary
            BookingStatus.COMPLETED -> "Completed" to MaterialTheme.colorScheme.primaryContainer
            BookingStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.error
            BookingStatus.NO_SHOW -> "No Show" to MaterialTheme.colorScheme.errorContainer
        }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.SM, vertical = Spacing.XS),
        )
    }
}

/**
 * Formats an Instant to HH:mm time string.
 */
private fun formatTime(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(localDateTime.hour, localDateTime.minute)
}
