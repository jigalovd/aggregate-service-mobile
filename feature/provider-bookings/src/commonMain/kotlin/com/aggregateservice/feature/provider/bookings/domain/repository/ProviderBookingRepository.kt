package com.aggregateservice.feature.provider.bookings.domain.repository

import com.aggregateservice.feature.provider.bookings.domain.model.ProviderBooking

/**
 * Repository interface для управления бронированиями провайдера.
 *
 * Определяет контракт для получения списка бронирований и выполнения
 * действий над ними (принять/отклонить/отменить).
 *
 * **Architecture:**
 * - Domain layer (этот интерфейс) не зависит от Data layer
 * - ScreenModels используют этот интерфейс
 * - Data layer предоставляет реализацию через DI
 */
interface ProviderBookingRepository {
    /**
     * Получает список бронирований провайдера с фильтрацией и пагинацией.
     *
     * Backend извлекает provider_id из JWT токена.
     *
     * @param status Фильтр по статусу (null = все статусы)
     * @param page Номер страницы (начиная с 1)
     * @param pageSize Размер страницы
     * @return Result со списком бронирований или ошибкой
     */
    suspend fun getProviderBookings(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<ProviderBooking>>

    /**
     * Принимает бронирование (изменяет статус на CONFIRMED).
     *
     * @param bookingId ID бронирования
     * @return Result с подтверждением или ошибкой
     */
    suspend fun acceptBooking(bookingId: String): Result<Unit>

    /**
     * Отклоняет бронирование (изменяет статус на CANCELLED).
     *
     * @param bookingId ID бронирования
     * @param reason Причина отклонения
     * @return Result с подтверждением или ошибкой
     */
    suspend fun rejectBooking(bookingId: String, reason: String): Result<Unit>

    /**
     * Отменяет бронирование (изменяет статус на CANCELLED).
     *
     * @param bookingId ID бронирования
     * @param reason Причина отмены (опционально)
     * @return Result с подтверждением или ошибкой
     */
    suspend fun cancelBooking(bookingId: String, reason: String? = null): Result<Unit>
}
