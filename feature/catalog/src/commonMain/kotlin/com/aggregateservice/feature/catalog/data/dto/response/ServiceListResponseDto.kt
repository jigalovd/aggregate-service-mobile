package com.aggregateservice.feature.catalog.data.dto.response

import com.aggregateservice.feature.catalog.data.dto.ServiceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceListResponseDto(
    @SerialName("services") val services: List<ServiceDto>,
    @SerialName("total") val total: Int,
)
