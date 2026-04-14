package com.aggregateservice.feature.booking.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingStatus
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.presentation.component.RescheduleBottomSheet
import com.aggregateservice.feature.booking.presentation.model.BookingDetailUiState
import com.aggregateservice.feature.booking.presentation.model.RescheduleSheetState
import com.aggregateservice.feature.booking.presentation.screenmodel.BookingDetailScreenModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

data class BookingDetailScreen(
    val bookingId: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<BookingDetailScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        LaunchedEffect(bookingId) {
            screenModel.loadBooking(bookingId)
        }

        BookingDetailScreenContent(
            uiState = uiState,
            rescheduleState = screenModel.rescheduleState.collectAsState().value,
            i18nProvider = i18nProvider,
            onBackClick = { navigator.pop() },
            onCancelClick = { reason -> screenModel.cancelBooking(reason) },
            onRetry = screenModel::retry,
            onLoadSlots = { providerId, date, serviceIds ->
                screenModel.loadSlots(providerId, date, serviceIds)
            },
            onSelectSlot = { slot -> screenModel.selectSlot(slot) },
            onSubmitReschedule = { reason ->
                screenModel.submitReschedule(bookingId, reason)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreenContent(
    uiState: BookingDetailUiState,
    rescheduleState: RescheduleSheetState,
    i18nProvider: I18nProvider,
    onBackClick: () -> Unit,
    onCancelClick: (String) -> Unit,
    onRetry: () -> Unit,
    onLoadSlots: (String, kotlinx.datetime.LocalDate, List<String>) -> Unit,
    onSelectSlot: (TimeSlot) -> Unit,
    onSubmitReschedule: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRescheduleSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.cancelError) {
        uiState.cancelError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error.message ?: i18nProvider[StringKey.ERROR])
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(i18nProvider[StringKey.Booking.TITLE]) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.booking == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error!!.message ?: i18nProvider[StringKey.ERROR],
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(Spacing.MD))
                        OutlinedButton(onClick = onRetry) {
                            Text(i18nProvider[StringKey.RETRY])
                        }
                    }
                }
            }
            uiState.booking != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.MD),
                ) {
                    val booking = uiState.booking

                    // Status chip
                    Surface(
                        color = statusColor(booking.status),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = statusLabel(booking.status, i18nProvider),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.MD))

                    // Provider name
                    Text(
                        text = booking.providerName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(Spacing.MD))

                    // Date/time
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.MD)) {
                            Text(
                                text = i18nProvider[StringKey.Booking.SELECT_DATE_TIME],
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Text(
                                text = formatBookingDateTime(booking),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Text(
                                text = booking.formattedDuration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.MD))

                    // Services
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.MD)) {
                            Text(
                                text = i18nProvider[StringKey.Booking.SERVICES],
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Spacer(modifier = Modifier.height(Spacing.SM))
                            booking.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(text = item.serviceName, modifier = Modifier.weight(1f))
                                    Text(text = "%.0f %s".format(item.price, booking.currency))
                                }
                                Spacer(modifier = Modifier.height(Spacing.XS))
                            }
                            Spacer(modifier = Modifier.height(Spacing.SM))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = i18nProvider[StringKey.Booking.TOTAL],
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = booking.formattedTotalPrice,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // Notes
                    booking.notes?.let { notes ->
                        Spacer(modifier = Modifier.height(Spacing.MD))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(Spacing.MD)) {
                                Text(
                                    text = i18nProvider[StringKey.Booking.NOTES],
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(modifier = Modifier.height(Spacing.XS))
                                Text(text = notes, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Action buttons
                    if (booking.canCancel || booking.canReschedule) {
                        Spacer(modifier = Modifier.height(Spacing.LG))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.MD),
                        ) {
                            if (booking.canReschedule) {
                                OutlinedButton(
                                    onClick = { showRescheduleSheet = true },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(i18nProvider[StringKey.Booking.RESCHEDULE])
                                }
                            }
                            if (booking.canCancel) {
                                OutlinedButton(
                                    onClick = { showCancelDialog = true },
                                    enabled = !uiState.isCancelling,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    if (uiState.isCancelling) {
                                        CircularProgressIndicator(modifier = Modifier.height(16.dp))
                                    } else {
                                        Text(i18nProvider[StringKey.Booking.CANCEL])
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.XL))
                }
            }
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(i18nProvider[StringKey.Booking.CANCEL_CONFIRMATION]) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelClick("")
                    },
                ) {
                    Text(i18nProvider[StringKey.Booking.CANCEL])
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text(i18nProvider[StringKey.Booking.DISMISS])
                }
            },
        )
    }

    // Reschedule bottom sheet
    if (showRescheduleSheet) {
        val booking = uiState.booking
        if (booking != null) {
            RescheduleBottomSheet(
                providerId = booking.providerId,
                state = rescheduleState,
                i18nProvider = i18nProvider,
                onDateSelected = { date ->
                    onLoadSlots(booking.providerId, date, booking.items.map { it.serviceId })
                },
                onSlotSelected = { slot -> onSelectSlot(slot) },
                onSubmit = { reason ->
                    showRescheduleSheet = false
                    onSubmitReschedule(reason)
                },
                onDismiss = { showRescheduleSheet = false },
            )
        }
    }
}

private fun statusColor(status: BookingStatus): androidx.compose.ui.graphics.Color =
    when (status) {
        BookingStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFFFA000)
        BookingStatus.CONFIRMED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        BookingStatus.IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        BookingStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
        BookingStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFFF44336)
        BookingStatus.EXPIRED -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
        BookingStatus.NO_SHOW -> androidx.compose.ui.graphics.Color(0xFFFF5722)
    }

private fun statusLabel(status: BookingStatus, i18nProvider: I18nProvider): String =
    when (status) {
        BookingStatus.PENDING -> i18nProvider[StringKey.Booking.STATUS_PENDING]
        BookingStatus.CONFIRMED -> i18nProvider[StringKey.Booking.STATUS_CONFIRMED]
        BookingStatus.IN_PROGRESS -> i18nProvider[StringKey.Booking.STATUS_IN_PROGRESS]
        BookingStatus.COMPLETED -> i18nProvider[StringKey.Booking.STATUS_COMPLETED]
        BookingStatus.CANCELLED -> i18nProvider[StringKey.Booking.STATUS_CANCELLED]
        BookingStatus.EXPIRED -> i18nProvider[StringKey.Booking.STATUS_EXPIRED]
        BookingStatus.NO_SHOW -> i18nProvider[StringKey.Booking.STATUS_NO_SHOW]
    }

private fun formatBookingDateTime(booking: Booking): String {
    val instant = booking.startTime
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
