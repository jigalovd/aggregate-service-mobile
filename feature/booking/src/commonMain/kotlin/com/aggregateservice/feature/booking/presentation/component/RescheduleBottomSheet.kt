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
import androidx.compose.material3.Surface
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
import com.aggregateservice.core.theme.Spacing
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class RescheduleSheetState(
    val selectedDate: LocalDate? = null,
    val slots: List<com.aggregateservice.feature.booking.domain.model.TimeSlot> = emptyList(),
    val selectedSlot: com.aggregateservice.feature.booking.domain.model.TimeSlot? = null,
    val isLoading: Boolean = false,
    val error: com.aggregateservice.core.network.AppError? = null,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RescheduleBottomSheet(
    providerId: String,
    state: RescheduleSheetState,
    onDateSelected: (LocalDate) -> Unit,
    onSlotSelected: (com.aggregateservice.feature.booking.domain.model.TimeSlot) -> Unit,
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
                text = "Reschedule booking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            // Date picker — next 30 days
            DateSelector(
                selectedDate = state.selectedDate,
                onDateSelected = onDateSelected,
            )

            // Slots grid
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text(
                    text = state.error!!.message ?: "Error loading slots",
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (state.slots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.MD))
                Text("Available times:", style = MaterialTheme.typography.titleSmall)
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else if (state.selectedDate != null) {
                Spacer(modifier = Modifier.height(Spacing.SM))
                Text("No available slots", style = MaterialTheme.typography.bodyMedium)
            }

            // Optional reason
            Spacer(modifier = Modifier.height(Spacing.MD))
            TextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(Spacing.MD))

            // Submit button
            Button(
                onClick = { onSubmit(reason) },
                enabled = state.selectedSlot != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reschedule")
            }

            Spacer(modifier = Modifier.height(Spacing.LG))
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Column {
        Text("Select date:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(Spacing.SM))

        // Show next 7 days as quick options
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.SM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SM),
        ) {
            (0..6).forEach { offset ->
                val date = today.plus(kotlinx.datetime.DatePeriod(days = offset))
                val isSelected = selectedDate == date
                OutlinedButton(
                    onClick = { onDateSelected(date) },
                ) {
                    Text(
                        text = "${date.dayOfMonth}.${date.monthNumber}",
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
