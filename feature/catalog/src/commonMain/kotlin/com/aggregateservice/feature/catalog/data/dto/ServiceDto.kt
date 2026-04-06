package com.aggregateservice.feature.catalog.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для response API услуг (GET /providers/{id}/services).
 *
 * **Important:** Этот класс содержит платформенные зависимости (Ktor).
 * Используется только в data слое, для преобразования в domain модели.
 *
 * @property id Уникальный идентификатор услуги
 * @property providerId ID мастера
 * @property categoryId ID категории
 * @property categoryNameMap Название категории (i18n map from backend I18nStringSchema)
 * @property titleMap Название услуги (i18n map from backend I18nStringSchema)
 * @property descriptionMap Описание услуги (i18n map from backend I18nStringSchema)
 * @property priceInCents Цена услуги в центах (backend returns cents)
 * @property durationMinutes Длительность в минутах
 * @property isActive Активна ли услуга
 * @property createdAt Дата создания услуги
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
    @SerialName("created_at") val createdAt: Instant,
) {
    /**
     * Extracts display name from I18nStringSchema.
     * Priority: ru -> he -> en
     */
    val name: String
        get() = titleMap["ru"] ?: titleMap["he"] ?: titleMap["en"] ?: ""

    /**
     * Extracts display description from I18nStringSchema.
     * Priority: ru -> he -> en
     */
    val description: String?
        get() = descriptionMap?.get("ru") ?: descriptionMap?.get("he") ?: descriptionMap?.get("en")
}
