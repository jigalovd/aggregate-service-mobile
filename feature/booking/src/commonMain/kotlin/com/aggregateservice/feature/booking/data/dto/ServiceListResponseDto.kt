package com.aggregateservice.feature.booking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceListResponseDto(
    @SerialName("services") val services: List<ServiceDto>,
    @SerialName("total") val total: Int,
)
