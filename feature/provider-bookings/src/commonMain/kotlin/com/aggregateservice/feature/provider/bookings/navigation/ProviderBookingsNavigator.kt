package com.aggregateservice.feature.provider.bookings.navigation

import cafe.adriel.voyager.core.screen.Screen

/**
 * Navigator interface для экрана управления бронированиями провайдера.
 *
 * Определяет контракт для навигации к экрану бронирований.
 * Интерфейс будет использоваться для обновления ProviderNavigator в core:navigation.
 */
interface ProviderBookingsNavigator {
    /**
     * Создаёт экран управления бронированиями.
     *
     * @return Screen для экрана бронирований
     */
    fun createBookingsScreen(): Screen
}