package com.aggregateservice.feature.booking.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для response API услуг при бронировании (GET /providers/{id}/services).
 *
 * **Feature Isolation:** Собственный DTO для booking, не зависит от catalog.
 * Содержит только поля, необходимые для бронирования.
 *
 * @property id Уникальный идентификатор услуги
 * @property name Название услуги
 * @property description Описание услуги
 * @property price Цена услуги
 * @property currency Код валюты
 * @property durationMinutes Длительность в минутах
 */
@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val currency: String = "ILS",
    @SerialName("durationMinutes")
    val durationMinutes: Int,
    @SerialName("isCombinable")
    val isCombinable: Boolean = true,
)
