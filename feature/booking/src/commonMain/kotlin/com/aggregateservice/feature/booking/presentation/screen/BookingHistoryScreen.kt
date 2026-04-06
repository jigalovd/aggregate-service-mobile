package com.aggregateservice.feature.booking.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.theme.Spacing
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.presentation.screenmodel.BookingHistoryScreenModel
import org.koin.compose.koinInject

/**
 * Voyager Screen для истории бронирований.
 */
object BookingHistoryScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<BookingHistoryScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        LaunchedEffect(Unit) {
            screenModel.loadBookings()
        }

        BookingHistoryScreenContent(
            i18nProvider = i18nProvider,
            uiState = uiState,
            onRefresh = { screenModel.refresh() },
            onCancelBooking = { bookingId, reason ->
                screenModel.cancelBooking(bookingId, reason)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreenContent(
    i18nProvider: I18nProvider,
    uiState: com.aggregateservice.feature.booking.presentation.model.BookingHistoryUiState,
    onRefresh: () -> Unit,
    onCancelBooking: (String, String?) -> Unit,
) {
    Scaffold(
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${i18nProvider[StringKey.ERROR]}: ${uiState.error?.message}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            uiState.isEmpty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = i18nProvider[StringKey.Booking.NO_BOOKINGS],
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = i18nProvider[StringKey.Booking.NO_BOOKINGS],
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = Spacing.MD),
                ) {
                    // Upcoming bookings
                    if (uiState.upcomingBookings.isNotEmpty()) {
                        item {
                            Text(
                                text = i18nProvider[StringKey.Booking.UPCOMING],
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = Spacing.SM),
                            )
                        }
                        items(uiState.upcomingBookings, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                onCancel = { onCancelBooking(booking.id, null) },
                                i18nProvider = i18nProvider,
                            )
                        }
                    }

                    // Past bookings
                    if (uiState.pastBookings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Spacing.MD))
                            Text(
                                text = i18nProvider[StringKey.Booking.PAST],
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = Spacing.SM),
                            )
                        }
                        items(uiState.pastBookings, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                onCancel = null,
                                i18nProvider = i18nProvider,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onCancel: (() -> Unit)?,
    i18nProvider: I18nProvider,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.SM),
    ) {
        Column(modifier = Modifier.padding(Spacing.MD)) {
            // Provider name
            Text(
                text = booking.providerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(Spacing.XS))

            // Status chip
            StatusChip(status = booking.status, i18nProvider = i18nProvider)

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Services summary
            Text(
                text = booking.servicesSummary,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(Spacing.SM))

            // Price and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Text(
                    text = booking.formattedTotalPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = booking.formattedDuration,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Cancel button (if applicable)
            if (onCancel != null && booking.canCancel) {
                Spacer(modifier = Modifier.height(Spacing.SM))
                androidx.compose.material3.TextButton(onClick = onCancel) {
                    Text(
                        text = i18nProvider[StringKey.Booking.CANCEL],
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus, i18nProvider: I18nProvider) {
    val (text, color) = when (status) {
        BookingStatus.PENDING -> i18nProvider[StringKey.Booking.STATUS_PENDING] to MaterialTheme.colorScheme.tertiary
        BookingStatus.CONFIRMED -> i18nProvider[StringKey.Booking.STATUS_CONFIRMED] to MaterialTheme.colorScheme.primary
        BookingStatus.IN_PROGRESS -> i18nProvider[StringKey.Booking.STATUS_IN_PROGRESS] to MaterialTheme.colorScheme.secondary
        BookingStatus.COMPLETED -> i18nProvider[StringKey.Booking.STATUS_COMPLETED] to MaterialTheme.colorScheme.primaryContainer
        BookingStatus.CANCELLED -> i18nProvider[StringKey.Booking.STATUS_CANCELLED] to MaterialTheme.colorScheme.error
        BookingStatus.EXPIRED -> i18nProvider[StringKey.Booking.STATUS_EXPIRED] to MaterialTheme.colorScheme.outline
        BookingStatus.NO_SHOW -> i18nProvider[StringKey.Booking.STATUS_NO_SHOW] to MaterialTheme.colorScheme.errorContainer
    }

    androidx.compose.material3.Surface(
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
