@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.aggregateservice.feature.booking.domain.repository

import com.aggregateservice.feature.booking.domain.model.Booking
import com.aggregateservice.feature.booking.domain.model.BookingService
import com.aggregateservice.feature.booking.domain.model.TimeSlot
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Repository interface для бронирований.
 *
 * Определяет контракт для работы с бронированиями.
 * Реализация находится в data слое (BookingRepositoryImpl).
 *
 * **Architecture:**
 * - Domain layer (этот интерфейс) не зависит от Data layer
 * - UseCases используют этот интерфейс
 * - Data layer предоставляет реализацию через DI
 */
interface BookingRepository {
    /**
     * Создаёт новое бронирование.
     *
     * @param providerId ID мастера
     * @param serviceIds Список ID услуг для бронирования
     * @param startTime Время начала бронирования
     * @param notes Заметки клиента (опционально)
     * @return Result с созданным бронированием или ошибкой
     */
    suspend fun createBooking(
        providerId: String,
        serviceIds: List<String>,
        startTime: Instant,
        notes: String?,
    ): Result<Booking>

    /**
     * Получает бронирование по ID.
     *
     * @param bookingId ID бронирования
     * @return Result с бронированием или ошибка NotFound
     */
    suspend fun getBookingById(bookingId: String): Result<Booking>

    /**
     * Получает историю бронирований клиента.
     *
     * Backend извлекает user_id из JWT токена.
     *
     * @param status Фильтр по статусу (опционально)
     * @param page Номер страницы (начиная с 1)
     * @param pageSize Размер страницы
     * @return Result со списком бронирований
     */
    suspend fun getClientBookings(
        status: String?,
        page: Int,
        pageSize: Int,
    ): Result<List<Booking>>

    /**
     * Подтверждает бронирование (только для мастера).
     *
     * @param bookingId ID бронирования
     * @return Result с обновлённым бронированием
     */
    suspend fun confirmBooking(bookingId: String): Result<Booking>

    /**
     * Отменяет бронирование.
     *
     * @param bookingId ID бронирования
     * @param reason Причина отмены (опционально)
     * @return Result с обновлённым бронированием
     */
    suspend fun cancelBooking(bookingId: String, reason: String?): Result<Booking>

    /**
     * Переносит бронирование на другое время.
     *
     * @param bookingId ID бронирования
     * @param newStartTime Новое время начала
     * @return Result с обновлённым бронированием
     */
    suspend fun rescheduleBooking(
        bookingId: String,
        newStartTime: Instant,
    ): Result<Booking>

    /**
     * Получает доступные временные слоты для бронирования.
     *
     * @param providerId ID мастера
     * @param date Дата для поиска слотов
     * @param serviceIds Список ID услуг (для вычисления длительности)
     * @return Result со списком доступных слотов
     */
    suspend fun getAvailableSlots(
        providerId: String,
        date: LocalDate,
        serviceIds: List<String>,
    ): Result<List<TimeSlot>>

    /**
     * Получает список услуг мастера для бронирования.
     *
     * **Feature Isolation:** Booking использует собственный метод вместо
     * зависимости от feature:catalog. См. docs/architecture/FEATURE_ISOLATION.md.
     *
     * @param providerId ID мастера
     * @return Result со списком услуг для бронирования
     */
    suspend fun getProviderServices(providerId: String): Result<List<BookingService>>
}
