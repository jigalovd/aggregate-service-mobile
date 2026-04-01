package com.aggregateservice.feature.catalog.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
)
