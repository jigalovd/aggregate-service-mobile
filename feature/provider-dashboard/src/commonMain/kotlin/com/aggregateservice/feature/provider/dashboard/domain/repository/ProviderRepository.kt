package com.aggregateservice.feature.provider.dashboard.domain.repository

import com.aggregateservice.feature.provider.dashboard.domain.model.DashboardBooking
import com.aggregateservice.feature.provider.dashboard.domain.model.EarningsSummary
import com.aggregateservice.feature.provider.dashboard.domain.model.ProviderStats

/**
 * Repository interface для дашборда провайдера.
 *
 * Определяет контракт для получения данных дашборда.
 * Реализация находится в data слое и использует существующие
 * API endpoints booking/catalog как proxy до появления
 * dedicated provider dashboard endpoints.
 *
 * **Architecture:**
 * - Domain layer (этот интерфейс) не зависит от Data layer
 * - ScreenModels используют этот интерфейс
 * - Data layer предоставляет реализацию через DI
 */
interface ProviderRepository {
    /**
     * Получает список бронирований на сегодня.
     *
     * Backend извлекает provider_id из JWT токена.
     *
     * @return Result со списком бронирований или ошибкой
     */
    suspend fun getTodaysBookings(): Result<List<DashboardBooking>>

    /**
     * Получает summary заработков провайдера.
     *
     * @return Result со summary или ошибкой
     */
    suspend fun getEarningsSummary(): Result<EarningsSummary>

    /**
     * Получает статистику провайдера.
     *
     * @return Result со статистикой или ошибкой
     */
    suspend fun getProviderStats(): Result<ProviderStats>
}