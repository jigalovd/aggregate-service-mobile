@file:OptIn(kotlin.time.ExperimentalTime::class)

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.presentation.screenmodel.BookingConfirmationScreenModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

/**
 * Voyager Screen для подтверждения бронирования.
 *
 * @property providerId ID мастера
 * @property providerName Название бизнеса мастера
 * @property serviceIds Список ID выбранных услуг
 * @property selectedDate Выбранная дата (ISO format)
 * @property slotStartTime Время начала слота (ISO format)
 */
data class BookingConfirmationScreen(
    val providerId: String,
    val providerName: String,
    val serviceIds: List<String>,
    val selectedDate: String,
    val slotStartTime: String,
    val services: List<BookingService> = emptyList(),
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<BookingConfirmationScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        // Load services and initialize via ScreenModel
        LaunchedEffect(providerId, serviceIds) {
            val startInstant = Instant.parse(slotStartTime)
            val slot =
                TimeSlot(
                    startTime = startInstant,
                    endTime = Instant.fromEpochMilliseconds(startInstant.toEpochMilliseconds() + 60 * 60 * 1000),
                    isAvailable = true,
                    providerId = providerId,
                )
            screenModel.initialize(
                providerId = providerId,
                providerName = providerName,
                services = services,
                selectedDate = LocalDate.parse(selectedDate),
                selectedSlot = slot,
            )
            // Fallback: load from API only when services not passed through navigation
            if (services.isEmpty()) {
                screenModel.loadServices(providerId, serviceIds)
            }
        }

        BookingConfirmationScreenContent(
            i18nProvider = i18nProvider,
            providerName = providerName,
            uiState = uiState,
            onNotesChange = screenModel::updateNotes,
            onSubmit = screenModel::submitBooking,
            onBack = { navigator.pop() },
            onDone = {
                val bookingId = uiState.booking?.id ?: return@BookingConfirmationScreenContent
                navigator.popUntilRoot()
                navigator.push(BookingDetailScreen(bookingId))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreenContent(
    i18nProvider: I18nProvider,
    providerName: String,
    uiState: com.aggregateservice.feature.booking.presentation.model.BookingConfirmationUiState,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    Scaffold { paddingValues ->
        when {
            uiState.isBooked -> {
                // Success state
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.MD),
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = i18nProvider[StringKey.Confirmation.BOOKING_CONFIRMED],
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        uiState.booking?.let { booking ->
                            Text(
                                text = "${booking.formattedTotalPrice}\n${booking.startTime}",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.MD))
                        Button(onClick = onDone) {
                            Text(i18nProvider[StringKey.DONE])
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(Spacing.MD),
                ) {
                    // Provider info
                    Text(
                        text = providerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(Spacing.MD))

                    // Booking summary card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.MD)) {
                            Text(
                                text = i18nProvider[StringKey.Confirmation.REVIEW_DETAILS],
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(Spacing.SM))

                            // Show service names in expandable section
                            if (uiState.services.isNotEmpty()) {
                                Text(
                                    text = i18nProvider[StringKey.Booking.SERVICES],
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.height(Spacing.XS))
                                uiState.services.forEach { service ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = service.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = service.formattedPrice,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(Spacing.SM))
                                // Total row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "${uiState.services.size} service${if (uiState.services.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = uiState.formattedTotal,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(i18nProvider[StringKey.Booking.SERVICES], style = MaterialTheme.typography.bodyMedium)
                                    Text("${uiState.services.size}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(i18nProvider[StringKey.Booking.DURATION], style = MaterialTheme.typography.bodyMedium)
                                Text(uiState.formattedDuration, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(Spacing.XS))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    i18nProvider[StringKey.SORT],
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    uiState.formattedTotal,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.MD))

                    // Notes input
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = onNotesChange,
                        label = { Text(i18nProvider[StringKey.Booking.NOTES]) },
                        placeholder = { Text(i18nProvider[StringKey.Booking.NOTES_PLACEHOLDER]) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Submit button
                    Button(
                        onClick = onSubmit,
                        enabled = uiState.canSubmit,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = Spacing.SM),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Text(if (uiState.isSubmitting) i18nProvider[StringKey.LOADING] else i18nProvider[StringKey.Booking.CONFIRM])
                    }

                    // Error display
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(Spacing.SM))
                        Text(
                            text = error.message ?: i18nProvider[StringKey.Error.UNKNOWN],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
