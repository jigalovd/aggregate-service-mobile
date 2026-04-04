package com.aggregateservice.feature.catalog.data.dto

import com.aggregateservice.feature.catalog.data.dto.response.LocationDto
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
 * @property location Локация мастера
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
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val businessName: String,
    @SerialName("bio") val description: String? = null,
    @SerialName("avatar_url") val logoUrl: String? = null,
    val photos: List<String> = emptyList(),
    val location: LocationDto? = null,
    @SerialName("rating_cached") val rating: Double = 0.0,
    @SerialName("reviews_count") val reviewCount: Int = 0,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Instant,
    val address: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val categories: List<CategoryDto> = emptyList(),
    val servicesCount: Int = 0,
    val workingHours: WorkingHoursDto? = null,
)
