package com.aggregateservice.core.navigation

import cafe.adriel.voyager.core.screen.Screen

/**
 * Интерфейс для навигации к booking flow.
 *
 * Решает проблему циклической зависимости между feature:catalog и feature:booking.
 * Реализация предоставляется в :feature:booking через DI.
 *
 * **Usage:**
 * ```kotlin
 * // В CatalogScreen
 * val bookingNavigator = koinGet<BookingNavigator>()
 * bookingNavigator.navigateToSelectService(providerId, providerName)
 * ```
 */
interface BookingNavigator {
    /**
     * Создаёт экран выбора услуг для бронирования.
     *
     * @param providerId ID мастера
     * @param providerName Название бизнеса мастера
     * @return Voyager Screen для выбора услуг
     */
    fun createSelectServiceScreen(providerId: String, providerName: String): Screen
}
