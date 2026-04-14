package com.aggregateservice.feature.booking.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey
import com.aggregateservice.core.theme.Spacing
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import com.aggregateservice.feature.booking.presentation.model.RescheduleSheetState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RescheduleBottomSheet(
    providerId: String,
    state: RescheduleSheetState,
    i18nProvider: I18nProvider,
    onDateSelected: (LocalDate) -> Unit,
    onSlotSelected: (TimeSlot) -> Unit,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var reason by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.MD),
        ) {
            Text(
                text = i18nProvider[StringKey.Booking.RESCHEDULE_TITLE],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            DateSelector(
                selectedDate = state.selectedDate,
                onDateSelected = onDateSelected,
                i18nProvider = i18nProvider,
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text(
                    text = i18nProvider[StringKey.Booking.ERROR_LOADING_SLOTS],
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (state.slots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.MD))
                Text(
                    text = i18nProvider[StringKey.Scheduling.AVAILABLE_SLOTS],
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(Spacing.SM))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SM),
                ) {
                    state.slots.filter { it.isAvailable }.forEach { slot ->
                        val isSelected = state.selectedSlot == slot
                        OutlinedButton(
                            onClick = { onSlotSelected(slot) },
                        ) {
                            Text(
                                text = slot.formattedStartTime,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            } else if (state.selectedDate != null) {
                Spacer(modifier = Modifier.height(Spacing.SM))
                Text(
                    text = i18nProvider[StringKey.Scheduling.NO_SLOTS_AVAILABLE],
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.MD))
            TextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text(i18nProvider[StringKey.Booking.REASON_OPTIONAL]) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            Button(
                onClick = { onSubmit(reason) },
                enabled = state.selectedSlot != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(i18nProvider[StringKey.Booking.RESCHEDULE])
            }

            Spacer(modifier = Modifier.height(Spacing.LG))
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    i18nProvider: I18nProvider,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column {
        Text(
            text = i18nProvider[StringKey.Scheduling.SELECT_DATE],
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(Spacing.SM))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            (0..29).forEach { offset ->
                val date = today.plus(offset, DateTimeUnit.DAY)
                val isSelected = selectedDate == date
                OutlinedButton(
                    onClick = { onDateSelected(date) },
                ) {
                    Text(
                        text = "${date.day}.${date.monthNumber}",
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}
