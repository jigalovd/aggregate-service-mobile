package com.aggregateservice.feature.booking.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для временного слота.
 *
 * Соответствует ответу от backend API /bookings/slots.
 */
@Serializable
data class TimeSlotDto(
    @SerialName("start_time") val startTime: Instant,
    @SerialName("end_time") val endTime: Instant,
    @SerialName("is_available") val isAvailable: Boolean,
    @SerialName("provider_id") val providerId: String,
)
