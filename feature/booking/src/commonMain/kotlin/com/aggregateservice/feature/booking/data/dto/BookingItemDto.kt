package com.aggregateservice.feature.booking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для позиции в бронировании.
 *
 * Соответствует ответу от backend API.
 */
@Serializable
data class BookingItemDto(
    @SerialName("id") val id: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("service_name") val serviceName: String,
    @SerialName("price") val price: Double,
    @SerialName("currency") val currency: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
)
