package com.aggregateservice.feature.booking.domain.model

import kotlinx.datetime.Instant

/**
 * Доменная модель бронирования.
 *
 * Представляет полное бронирование одной или нескольких услуг
 * у конкретного мастера (provider).
 *
 * **Important:** Этот класс НЕ содержит никаких платформенных зависимостей
 * или DTO из network слоя. Только чистые бизнес-данные.
 *
 * @property id Уникальный идентификатор бронирования
 * @property providerId ID мастера
 * @property providerName Название бизнеса мастера (snapshot)
 * @property clientId ID клиента
 * @property startTime Время начала бронирования
 * @property endTime Время окончания бронирования (вычисляется из услуг)
 * @property status Текущий статус бронирования
 * @property items Список услуг в бронировании
 * @property totalPrice Общая стоимость всех услуг
 * @property totalDurationMinutes Общая длительность всех услуг
 * @property currency Код валюты
 * @property notes Заметки клиента к бронированию
 * @property createdAt Дата создания бронирования
 * @property updatedAt Дата последнего обновления
 */
data class Booking(
    val id: String,
    val providerId: String,
    val providerName: String,
    val clientId: String,
    val startTime: Instant,
    val endTime: Instant,
    val status: BookingStatus,
    val items: List<BookingItem>,
    val totalPrice: Double,
    val totalDurationMinutes: Int,
    val currency: String,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /**
     * Форматированная общая цена (например, "300 ₪").
     */
    val formattedTotalPrice: String
        get() = "%.0f %s".format(totalPrice, currency)

    /**
     * Форматированная общая длительность (например, "90 min").
     */
    val formattedDuration: String
        get() = "${totalDurationMinutes} min"

    /**
     * Является ли бронирование прошедшим (время окончания прошло).
     */
    val isPast: Boolean
        get() = endTime < Instant.fromEpochMilliseconds(System.currentTimeMillis())

    /**
     * Можно ли отменить бронирование.
     * Зависит от статуса и времени до начала (минимум 2 часа для клиента).
     */
    val canCancel: Boolean
        get() = status.isCancellable && !isPast

    /**
     * Можно ли перенести бронирование.
     */
    val canReschedule: Boolean
        get() = status.isReschedulable && !isPast

    /**
     * Количество услуг в бронировании.
     */
    val servicesCount: Int
        get() = items.size

    /**
     * Краткое описание услуг (первые 3 названия).
     */
    val servicesSummary: String
        get() = items.take(3).joinToString(", ") { it.serviceName }
            .let { if (items.size > 3) "$it +${items.size - 3} more" else it }

    companion object {
        /**
         * Создаёт пустое бронирование (для инициализации UI state).
         */
        fun empty() = Booking(
            id = "",
            providerId = "",
            providerName = "",
            clientId = "",
            startTime = Instant.fromEpochMilliseconds(0),
            endTime = Instant.fromEpochMilliseconds(0),
            status = BookingStatus.PENDING,
            items = emptyList(),
            totalPrice = 0.0,
            totalDurationMinutes = 0,
            currency = "ILS",
            notes = null,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
    }
}
