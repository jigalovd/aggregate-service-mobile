package com.aggregateservice.feature.booking.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для response API услуг при бронировании (GET /providers/{id}/services).
 *
 * Соответствует backend PublicProviderServiceItemResponse.
 * Содержит i18n-поля (Map<String, String>) для title/description,
 * которые маппятся в плоские строки в ServiceMapper.
 *
 * @property id Уникальный идентификатор ProviderService
 * @property titleMap Название услуги (i18n map: {ru, he, en})
 * @property descriptionMap Описание услуги (i18n map)
 * @property priceInCents Цена услуги (base_price из backend)
 * @property durationMinutes Длительность в минутах
 */
@Serializable
data class ServiceDto(
    val id: String,
    val providerId: String? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryNameMap: Map<String, String>,
    @SerialName("title") val titleMap: Map<String, String>,
    @SerialName("description") val descriptionMap: Map<String, String>? = null,
    @SerialName("base_price") val priceInCents: Int,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_combinable") val isCombinable: Boolean = true,
    @SerialName("created_at") val createdAt: Instant,
)
