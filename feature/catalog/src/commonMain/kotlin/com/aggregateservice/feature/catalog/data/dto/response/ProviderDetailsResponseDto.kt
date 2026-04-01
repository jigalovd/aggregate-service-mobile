package com.aggregateservice.feature.catalog.data.dto.response

import com.aggregateservice.feature.catalog.data.dto.ProviderDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderDetailsResponseDto(
    @SerialName("data") val data: ProviderDto,
)
