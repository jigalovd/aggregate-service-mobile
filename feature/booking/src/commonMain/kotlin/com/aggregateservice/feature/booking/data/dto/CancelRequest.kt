package com.aggregateservice.feature.booking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body для отмены бронирования.
 *
 * Отправляется на PATCH /bookings/{id}/cancel.
 */
@Serializable
data class CancelRequest(
    @SerialName("cancellation_reason") val cancellationReason: String? = null,
)
