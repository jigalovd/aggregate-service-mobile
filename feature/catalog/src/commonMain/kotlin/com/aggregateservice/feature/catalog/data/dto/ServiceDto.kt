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
 * @property name Название услуги (i18n)
 * @property description Описание услуги (i18n)
 * @property price Цена услуги
 * @property durationMinutes Длительность в минутах
 * @property isActive Активна ли услуга
 * @property createdAt Дата создания услуги
 */
@Serializable
data class ServiceDto(
    val id: String,
    val providerId: String,
    val categoryId: String,
    @SerialName("category_name")
    val categoryName: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("price") val price: Double,
    @SerialName("durationMinutes") val durationMinutes: Int,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("createdAt") val createdAt: Instant,
)
