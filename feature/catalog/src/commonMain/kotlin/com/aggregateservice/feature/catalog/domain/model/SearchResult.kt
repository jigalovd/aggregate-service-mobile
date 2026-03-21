package com.aggregateservice.feature.catalog.domain.model

/**
 * Результат поиска мастеров с пагинацией.
 *
 * @property providers Список мастеров
 * @property totalCount Общее количество результатов
 * @property totalPages Количество страниц
 * @property currentPage Текущая страница
 * @property hasMore Есть ли ещё результаты
 */
data class SearchResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val totalPages: Int,
    val currentPage: Int,
) {
    /**
     * Есть ли следующая страница.
     */
    val hasNextPage: Boolean
        get() = currentPage < totalPages

    /**
     * Есть ли предыдущая страница.
     */
    val hasPreviousPage: Boolean
        get() = currentPage > 1

    companion object {
        /**
         * Пустой результат.
         */
        fun <T> empty() = SearchResult<T>(
            items = emptyList(),
            totalCount = 0,
            totalPages = 0,
            currentPage = 1,
        )
    }
}
