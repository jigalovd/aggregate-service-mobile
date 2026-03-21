package com.aggregateservice.feature.catalog.domain.model

/**
 * Value Object для фильтров поиска мастеров.
 *
 * Используется для:
 * - Фильтрации списка мастеров по категории
 * - Геопоиска по радиусу
 * - Сортировки результатов
 * - Пагинации
 *
 * @property categoryIds Список ID категорий для фильтрации
 * @property latitude Широта центра поиска (для геопоиска)
 * @property longitude Долгота центра поиска (для геопоиска)
 * @property radiusKm Радиус поиска в километрах
 * @property minRating Минимальный рейтинг
 * @property isVerified Только верифицированные мастера
 * @property sortBy Поле сортировки
 * @property sortOrder Порядок сортировки (ASC/DESC)
 * @property page Номер страницы (для пагинации)
 * @property pageSize Размер страницы
 */
data class SearchFilters(
    val query: String? = null,
    val categoryIds: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusKm: Double? = null,
    val minRating: Double? = null,
    val isVerified: Boolean? = null,
    val sortBy: SortBy = SortBy.RATING,
    val sortOrder: SortOrder = SortOrder.DESC,
    val page: Int = 1,
    val pageSize: Int = 20,
) {
    /**
     * Является ли фильтр геопоиском.
     */
    val isGeoSearch: Boolean
        get() = latitude != null && longitude != null && radiusKm != null

    /**
     * Смещение для пагинации (page * pageSize).
     */
    val offset: Int
        get() = (page - 1) * pageSize

    /**
     * Поле сортировки.
     */
    enum class SortBy(val displayName: String) {
        RATING("Рейтинг"),
        REVIEW_COUNT("Отзывы"),
        DISTANCE("Расстояние"),
        CREATED_AT("Дата"),
        NAME("Название"),
    }

    /**
     * Порядок сортировки.
     */
    enum class SortOrder(val displayName: String) {
        ASC("По возрастанию"),
        DESC("По убыванию"),
    }

    companion object {
        /**
         * Пустые фильтры (по умолчанию).
         */
        val Empty = SearchFilters()
    }
}
