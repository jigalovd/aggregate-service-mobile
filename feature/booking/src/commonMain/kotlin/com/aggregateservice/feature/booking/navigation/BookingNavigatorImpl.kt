package com.aggregateservice.feature.booking.navigation

import cafe.adriel.voyager.core.screen.Screen
import com.aggregateservice.core.navigation.BookingNavigator
import com.aggregateservice.feature.booking.presentation.screen.SelectServiceScreen

/**
 * Реализация BookingNavigator для feature:booking.
 *
 * Создаёт экраны booking flow без прямой зависимости от feature:catalog.
 */
class BookingNavigatorImpl : BookingNavigator {
    override fun createSelectServiceScreen(providerId: String, providerName: String): Screen {
        return SelectServiceScreen(
            providerId = providerId,
            providerName = providerName,
        )
    }
}
