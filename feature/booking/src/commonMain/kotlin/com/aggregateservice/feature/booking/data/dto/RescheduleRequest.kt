package com.aggregateservice.feature.booking.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body для переноса бронирования.
 *
 * Отправляется на PATCH /bookings/{id}/reschedule.
 */
@Serializable
data class RescheduleRequest(
    @SerialName("new_start_time") val newStartTime: Instant,
)
