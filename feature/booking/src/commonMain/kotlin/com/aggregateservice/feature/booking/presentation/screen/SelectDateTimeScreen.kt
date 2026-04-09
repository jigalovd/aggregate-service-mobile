@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.presentation.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.presentation.screenmodel.SelectDateTimeScreenModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.compose.koinInject
import kotlin.time.Clock

/**
 * Voyager Screen для выбора даты и времени.
 *
 * @property providerId ID мастера
 * @property providerName Название бизнеса мастера
 * @property serviceIds Список ID выбранных услуг
 */
data class SelectDateTimeScreen(
    val providerId: String,
    val providerName: String,
    val serviceIds: List<String>,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SelectDateTimeScreenModel>()
        val uiState by screenModel.uiState.collectAsState()
        val i18nProvider: I18nProvider = koinInject()

        // Load available slots on first composition
        LaunchedEffect(providerId) {
            screenModel.loadAvailableSlots(providerId, serviceIds)
        }

        SelectDateTimeScreenContent(
            i18nProvider = i18nProvider,
            providerName = providerName,
            uiState = uiState,
            onSelectDate = screenModel::selectDate,
            onSelectSlot = screenModel::selectSlot,
            onContinue = {
                uiState.selectedSlot?.let { slot ->
                    uiState.selectedDate?.let { date ->
                        navigator.push(
                            BookingConfirmationScreen(
                                providerId = providerId,
                                providerName = providerName,
                                serviceIds = serviceIds,
                                selectedDate = date.toString(),
                                slotStartTime = slot.startTime.toString(),
                            ),
                        )
                    }
                }
            },
            onBack = { navigator.pop() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectDateTimeScreenContent(
    i18nProvider: I18nProvider,
    providerName: String,
    uiState: com.aggregateservice.feature.booking.presentation.model.SelectDateTimeUiState,
    onSelectDate: (LocalDate) -> Unit,
    onSelectSlot: (TimeSlot) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (uiState.hasSelection) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(Spacing.MD),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.selectedDate?.toString() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = uiState.selectedSlot?.formattedTimeRange ?: "",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Button(onClick = onContinue) {
                            Text(i18nProvider[StringKey.Booking.CONTINUE])
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier =
                        Modifier
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

            else -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(Spacing.MD),
                ) {
                    // Date selection header
                    Text(
                        text = i18nProvider[StringKey.Booking.SELECT_DATE],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(Spacing.SM))

                    // Booking horizon hint (US-3.34)
                    if (uiState.bookingHorizonVisible) {
                        Text(
                            text = "Бронирование возможно не более чем за ${uiState.maxAdvanceDays} дней",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.SM))
                    }

                    // Date chips (next 30 days maximum, US-3.34)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                        verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                    ) {
                        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        repeat(30) { offset ->
                            val date = today.plus(offset, DateTimeUnit.DAY)
                            DateChip(
                                date = date,
                                isSelected = uiState.selectedDate == date,
                                hasSlots = uiState.availableSlotsForDate.isNotEmpty(),
                                onClick = { onSelectDate(date) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.LG))

                    // Time slots
                    if (uiState.selectedDate != null) {
                        Text(
                            text = i18nProvider[StringKey.Scheduling.AVAILABLE_SLOTS],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(Spacing.SM))

                        if (uiState.availableSlotsForDate.isEmpty()) {
                            Text(
                                text = i18nProvider[StringKey.Scheduling.NO_SLOTS_AVAILABLE],
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                            ) {
                                items(uiState.availableSlotsForDate, key = { it.startTime.toString() }) { slot ->
                                    TimeSlotItem(
                                        slot = slot,
                                        isSelected = uiState.selectedSlot == slot,
                                        onClick = { onSelectSlot(slot) },
                                        i18nProvider = i18nProvider,
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(i18nProvider[StringKey.Booking.SELECT_DATE_TO_SEE])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    hasSlots: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        colors =
            if (isSelected) {
                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            } else {
                androidx.compose.material3.ButtonDefaults
                    .outlinedButtonColors()
            },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfWeek.name.take(3),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun TimeSlotItem(
    slot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    i18nProvider: I18nProvider,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = slot.isAvailable, onClick = onClick),
        colors =
            if (isSelected) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            } else if (!slot.isAvailable) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = slot.formattedTimeRange,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.width(Spacing.MD))
            if (!slot.isAvailable) {
                Text(
                    text = i18nProvider[StringKey.Booking.SLOT_BOOKED],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
