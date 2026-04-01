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
    val name: Map<String, String>,
    @SerialName("icon_url") val icon: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

/**
 * Wrapper DTO для response API списка категорий.
 * API возвращает {"categories": [...]} но Ktor ожидает тип для десериализации.
 */
@Serializable
data class CategoriesResponseDto(
    @SerialName("categories") val categories: List<CategoryDto>,
    @SerialName("total") val total: Int
)
