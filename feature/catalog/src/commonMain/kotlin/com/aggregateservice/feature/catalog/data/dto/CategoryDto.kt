package com.aggregateservice.feature.catalog.data.dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для response API категорий (GET /categories).
 *
 * **Important:** Этот класс содержит платформенные зависимости (Ktor).
 * Используется только в data слое, для преобразования в domain модели.
 *
 * @property id Уникальный идентификатор категории
 * @property name Название категории (i18n)
 * @property icon URL иконки
 * @property parentId ID родительской категории (null для корневой)
 * @property isActive Активна ли категория
 * @property sortOrder Порядок сортировки
 */
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String? = null,
    val parentId: String? = null,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("sortOrder") val sortOrder: Int = 0,
)
