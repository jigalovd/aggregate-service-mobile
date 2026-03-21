package com.aggregateservice.feature.catalog.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для ответ API провайдеров (GET /providers).
 *
 * **Important:** Этот класс содержит платформенные зависимости (Ktor).
 * Используется только в data слое, для преобразования в domain модели.
 *
 * @property id Уникальный идентификатор мастера
 * @property userId ID пользователя (owner)
 * @property businessName Название бизнеса
 * @property description Описание бизнеса (i18n)
 * @property logoUrl URL логотипа
 * @property photos URL фотографий
 * @property rating Средний рейтинг
 * @property reviewCount Количество отзывов
 * @property latitude Широта
 * @property longitude Долгота
 * @property address Адрес
 * @property city Город
 * @property postalCode Почтовый индекс
 * @property country Страна
 * @property isVerified Подтверждён ли верифициров
 * @property isActive Активен ли профиль
 * @property createdAt Дата создания профиля
 * @property categories Список категорий услуг
 * @property servicesCount Количество услуг
 */
@Serializable
data class ProviderDto(
    val id: String,
    val userId: String,
    val businessName: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val photos: List<String> = emptyList(),
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("reviewCount") val reviewCount: Int = 0,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("address") val address: String,
    @SerialName("city") val city: String,
    @SerialName("postalCode") val postalCode: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("isVerified") val isVerified: Boolean = false,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("createdAt") val createdAt: Instant,
    @SerialName("categories") val categories: List<CategoryDto> = emptyList(),
    @SerialName("servicesCount") val servicesCount: Int = 0,
    @SerialName("workingHours") val workingHours: WorkingHoursDto? = null,
)
