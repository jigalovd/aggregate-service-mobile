package com.aggregateservice.feature.provider.bookings.navigation

import cafe.adriel.voyager.core.screen.Screen
import com.aggregateservice.feature.provider.bookings.presentation.screen.ProviderBookingsScreen

/**
 * Реализация ProviderBookingsNavigator для feature:provider-bookings.
 */
class ProviderBookingsNavigatorImpl : ProviderBookingsNavigator {
    override fun createBookingsScreen(): Screen {
        return ProviderBookingsScreen
    }
}
