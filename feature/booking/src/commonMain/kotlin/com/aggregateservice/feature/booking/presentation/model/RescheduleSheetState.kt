package com.aggregateservice.feature.booking.presentation.model

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import kotlinx.datetime.LocalDate

data class RescheduleSheetState(
    val selectedDate: LocalDate? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedSlot: TimeSlot? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
)
