package com.aggregateservice.feature.provider.dashboard.domain.model

import kotlinx.datetime.Instant

/**
 * Доменная модель бронирования для дашборда провайдера.
 *
 * Содержит информацию о сегодняшних бронированиях, необходимую
 * для отображения в дашборде провайдера.
 *
 * **Note:** Это упрощённая модель, специфичная для dashboard.
 * BookingStatus определён локально для изоляции от feature:booking.
 *
 * @property id Уникальный идентификатор бронирования
 * @property clientName Имя клиента (snapshot, может быть анонимизировано)
 * @property startTime Время начала
 * @property endTime Время окончания
 * @property serviceName Название услуги (краткое, для отображения в списке)
 * @property status Текущий статус бронирования
 * @property totalPrice Общая стоимость бронирования
 */
data class DashboardBooking(
    val id: String,
    val clientName: String,
    val startTime: Instant,
    val endTime: Instant,
    val serviceName: String,
    val status: BookingStatus,
    val totalPrice: Double,
) {
    /**
     * Форматированная цена (например, "150 ₪").
     */
    val formattedPrice: String
        get() = "%.0f ₪".format(totalPrice)

    /**
     * Длительность в минутах.
     */
    val durationMinutes: Int
        get() {
            val diffMs = endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()
            return (diffMs / (1000 * 60)).toInt()
        }

    /**
     * Форматированная длительность (например, "60 min").
     */
    val formattedDuration: String
        get() = "$durationMinutes min"

    /**
     * Является ли бронирование активным (upcoming / in progress).
     */
    val isActive: Boolean
        get() = status.isActive && startTime > Instant.fromEpochMilliseconds(0)

    companion object {
        /**
         * Creates an empty placeholder for initialization.
         */
        fun empty() = DashboardBooking(
            id = "",
            clientName = "",
            startTime = Instant.fromEpochMilliseconds(0),
            endTime = Instant.fromEpochMilliseconds(0),
            serviceName = "",
            status = BookingStatus.PENDING,
            totalPrice = 0.0,
        )
    }
}

/**
 * Статус бронирования.
 *
 * Определён локально в provider-dashboard для изоляции от feature:booking.
 * Синхронизируется с BookingStatus в feature:booking.
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    /**
     * Является ли статус активным (бронирование ожидается или идёт).
     */
    val isActive: Boolean
        get() = this == PENDING || this == CONFIRMED || this == IN_PROGRESS

    /**
     * Можно ли отменить бронирование с этим статусом.
     */
    val isCancellable: Boolean
        get() = this == PENDING || this == CONFIRMED

    /**
     * Можно ли перенести бронирование с этим статусом.
     */
    val isReschedulable: Boolean
        get() = this == PENDING || this == CONFIRMED
}