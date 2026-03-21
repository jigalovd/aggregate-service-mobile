package com.aggregateservice.feature.catalog.domain.model

/**
 * Чистая доменная модель категории услуг (Category).
 *
 * Category - это группа услуг для классификации (например, "Стрижка", "Маникюр", "Окрашивание").
 *
 * **Important:** Этот класс НЕ содержит никаких платформенных зависимостей
 * или DTO из network слоя. Только чистые бизнес-данные.
 *
 * @property id Уникальный идентификатор категории
 * @property name Название категории (i18n)
 * @property icon URL иконки (опционально)
 * @property parentId ID родительской категории (для вложенных категорий)
 * @property isActive Активна ли категория
 * @property sortOrder Порядок сортировки
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String? = null,
    val parentId: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
) {
    /**
     * Является ли эта категория корневой (не имеет родителя).
     */
    val isRootCategory: Boolean
        get() = parentId == null
}
