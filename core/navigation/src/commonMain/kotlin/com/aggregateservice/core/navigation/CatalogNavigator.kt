package com.aggregateservice.core.navigation

import cafe.adriel.voyager.core.screen.Screen

/**
 * Интерфейс для навигации к каталогу (провайдеры).
 *
 * Решает проблему циклической зависимости между feature:catalog и feature:favorites.
 * Реализация предоставляется в :feature:catalog через DI.
 */
interface CatalogNavigator {
    /**
     * Создаёт экран каталога (главный экран).
     *
     * @return Voyager Screen для каталога
     */
    fun createCatalogScreen(): Screen

    /**
     * Создаёт экран деталей провайдера.
     *
     * @param providerId ID провайдера
     * @return Voyager Screen для деталей провайдера
     */
    fun createProviderDetailScreen(providerId: String): Screen
}
