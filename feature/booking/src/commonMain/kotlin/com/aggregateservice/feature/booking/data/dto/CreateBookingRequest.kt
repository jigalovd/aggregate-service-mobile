package com.aggregateservice.feature.booking.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body для создания бронирования.
 *
 * Отправляется на POST /bookings.
 */
@Serializable
data class CreateBookingRequest(
    @SerialName("provider_id") val providerId: String,
    @SerialName("service_ids") val serviceIds: List<String>,
    @SerialName("start_time") val startTime: Instant,
    @SerialName("notes") val notes: String? = null,
)
