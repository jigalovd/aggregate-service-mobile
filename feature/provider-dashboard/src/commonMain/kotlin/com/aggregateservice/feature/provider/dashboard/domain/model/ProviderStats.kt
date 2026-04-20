package com.aggregateservice.feature.provider.dashboard.domain.model

/**
 * Доменная модель статистики провайдера.
 *
 * Содержит базовые показатели для дашборда:
 * - количество ожидающих запросов
 * - количество активных бронирований
 * - количество завершённых сегодня
 *
 * @property pendingRequests Количество запросов, ожидающих подтверждения
 * @property activeBookings Количество активных (подтверждённых) бронирований
 * @property completedToday Количество завершённых бронирований сегодня
 */
data class ProviderStats(
    val pendingRequests: Int,
    val activeBookings: Int,
    val completedToday: Int,
) {
    /**
     * Общее количество бронирований для отображения.
     */
    val totalBookings: Int
        get() = pendingRequests + activeBookings + completedToday

    /**
     * Пустое состояние (для инициализации UI).
     */
    companion object {
        fun empty() = ProviderStats(
            pendingRequests = 0,
            activeBookings = 0,
            completedToday = 0,
        )
    }
}