package com.aggregateservice.feature.booking.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для бронирования.
 *
 * Соответствует ответу от backend API /bookings.
 */
@Serializable
data class BookingDto(
    @SerialName("id") val id: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("provider_name") val providerName: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("start_time") val startTime: Instant,
    @SerialName("end_time") val endTime: Instant,
    @SerialName("status") val status: String,
    @SerialName("items") val items: List<BookingItemDto>,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("total_duration_minutes") val totalDurationMinutes: Int,
    @SerialName("currency") val currency: String,
    @SerialName("notes") val notes: String? = null,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
)
