package com.aggregateservice.feature.catalog.data.dto.response

import com.aggregateservice.feature.catalog.data.dto.ProviderDto
import com.aggregateservice.feature.catalog.data.dto.ServiceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для composite endpoint GET /providers/{id}/detail.
 *
 * Возвращает provider + services + is_favorite в одном запросе,
 * заменяя 3 отдельных API вызова.
 */
@Serializable
data class ProviderCompositeDto(
    val provider: ProviderDto,
    val services: List<ServiceDto> = emptyList(),
    @SerialName("is_favorite") val isFavorite: Boolean? = null,
)
