package com.aggregateservice.feature.catalog.data.dto.response

import com.aggregateservice.feature.catalog.data.dto.ProviderDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderSearchResponseDto(
    @SerialName("providers") val providers: List<ProviderDto>,
    @SerialName("total") val total: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("offset") val offset: Int,
)
