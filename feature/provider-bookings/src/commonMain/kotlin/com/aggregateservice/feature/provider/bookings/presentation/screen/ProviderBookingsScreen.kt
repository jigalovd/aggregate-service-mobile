package com.aggregateservice.feature.provider.bookings.presentation.screen

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.provider.bookings.domain.model.BookingFilter
import com.aggregateservice.feature.provider.bookings.domain.model.BookingStatus
import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking
import com.aggregateservice.feature.provider.bookings.presentation.model.ProviderBookingsUiState
import com.aggregateservice.feature.provider.bookings.presentation.screenmodel.ProviderBookingsScreenModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Provider Bookings Voyager Screen.
 *
 * Displays all bookings for the provider with status filtering
 * and action capabilities (accept/reject/cancel).
 * Requires PROVIDER role to access.
 */
object ProviderBookingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ProviderBookingsScreenModel>()
        val uiState by screenModel.uiState.collectAsState()

        ProviderBookingsScreenContent(
            uiState = uiState,
            onFilterChange = { screenModel.filterBookings(it) },
            onRefresh = { screenModel.refresh() },
            onRetry = { screenModel.retry() },
            onAccept = { screenModel.acceptBooking(it) },
            onReject = { screenModel.rejectBooking(it, "") },
            onCancel = { screenModel.cancelBooking(it) },
            onClearActionError = { screenModel.clearActionError() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderBookingsScreenContent(
    uiState: ProviderBookingsUiState,
    onFilterChange: (BookingFilter) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onCancel: (String) -> Unit,
    onClearActionError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle action errors
    LaunchedEffect(uiState) {
        if (uiState is ProviderBookingsUiState.Content && uiState.actionError != null) {
            snackbarHostState.showSnackbar(uiState.actionError)
            onClearActionError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (uiState) {
            is ProviderBookingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProviderBookingsUiState.Error -> {
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

            is ProviderBookingsUiState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    // Status Filter Tabs
                    StatusFilterTabs(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = onFilterChange,
                    )

                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    ) {
                        if (uiState.isEmpty) {
                            EmptyBookingsContent(
                                selectedFilter = uiState.selectedFilter,
                            )
                        } else {
                            BookingsList(
                                bookings = uiState.bookings,
                                isLoadingAction = uiState.isLoadingAction,
                                onAccept = onAccept,
                                onReject = onReject,
                                onCancel = onCancel,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterTabs(
    selectedFilter: BookingFilter,
    onFilterSelected: (BookingFilter) -> Unit,
) {
    val filters = listOf(
        BookingFilter.ALL,
        BookingFilter.PENDING,
        BookingFilter.CONFIRMED,
        BookingFilter.IN_PROGRESS,
        BookingFilter.COMPLETED,
        BookingFilter.CANCELLED,
    )

    val selectedIndex = filters.indexOf(selectedFilter).coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth(),
    ) {
        filters.forEachIndexed { index, filter ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = filter.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun EmptyBookingsContent(
    selectedFilter: BookingFilter,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No bookings found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.SM))
            Text(
                text = when (selectedFilter) {
                    BookingFilter.ALL -> "You have no bookings yet"
                    BookingFilter.PENDING -> "No pending bookings"
                    BookingFilter.CONFIRMED -> "No confirmed bookings"
                    BookingFilter.IN_PROGRESS -> "No bookings in progress"
                    BookingFilter.COMPLETED -> "No completed bookings"
                    BookingFilter.CANCELLED -> "No cancelled bookings"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun BookingsList(
    bookings: List<ProviderBooking>,
    isLoadingAction: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onCancel: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MD),
    ) {
        items(bookings, key = { it.id }) { booking ->
            ProviderBookingCard(
                booking = booking,
                isLoadingAction = isLoadingAction,
                onAccept = { onAccept(booking.id) },
                onReject = { onReject(booking.id) },
                onCancel = { onCancel(booking.id) },
                modifier = Modifier.padding(vertical = Spacing.SM),
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(Spacing.LG))
        }
    }
}

@Composable
fun ProviderBookingCard(
    booking: ProviderBooking,
    isLoadingAction: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.MD),
        ) {
            // Header: Client name + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = booking.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(Spacing.SM))
                BookingStatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Service name
            Text(
                text = booking.serviceName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDateTime(booking.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Price and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = booking.formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = booking.formattedDuration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Action buttons for PENDING bookings
            if (booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(Spacing.MD))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = onReject,
                        enabled = !isLoadingAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        if (isLoadingAction) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError,
                            )
                        } else {
                            Text("Reject")
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.SM))
                    Button(
                        onClick = onAccept,
                        enabled = !isLoadingAction,
                    ) {
                        if (isLoadingAction) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("Accept")
                        }
                    }
                }
            }

            // Cancel button for CONFIRMED bookings
            if (booking.status == BookingStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(Spacing.MD))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isLoadingAction,
                    ) {
                        if (isLoadingAction) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Cancel")
                        }
                    }
                }
            }

            // Notes (if present)
            if (!booking.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Spacing.SM))
                Text(
                    text = "Notes: ${booking.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            // Phone (if present)
            if (!booking.clientPhone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Spacing.XS))
                Text(
                    text = "Phone: ${booking.clientPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun BookingStatusBadge(status: BookingStatus) {
    val (text, containerColor, contentColor) =
        when (status) {
            BookingStatus.PENDING -> Triple(
                "Pending",
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer,
            )
            BookingStatus.CONFIRMED -> Triple(
                "Confirmed",
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
            )
            BookingStatus.IN_PROGRESS -> Triple(
                "In Progress",
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
            )
            BookingStatus.COMPLETED -> Triple(
                "Completed",
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BookingStatus.CANCELLED -> Triple(
                "Cancelled",
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
            )
            BookingStatus.NO_SHOW -> Triple(
                "No Show",
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
            )
        }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = Spacing.SM, vertical = Spacing.XS),
        )
    }
}

/**
 * Formats an Instant to date string (e.g., "15 Jan 2024").
 */
private fun formatDateTime(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    return "${localDateTime.dayOfMonth} ${months[localDateTime.monthNumber - 1]} ${localDateTime.year}"
}

/**
 * Formats an Instant to HH:mm time string.
 */
private fun formatTime(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(localDateTime.hour, localDateTime.minute)
}
