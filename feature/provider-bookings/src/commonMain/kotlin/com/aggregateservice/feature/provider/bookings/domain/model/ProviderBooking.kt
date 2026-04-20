package com.aggregateservice.feature.provider.bookings.domain.model

import kotlinx.datetime.Instant

/**
 * Доменная модель бронирования для провайдера.
 *
 * Содержит полную информацию о бронировании, необходимую
 * для управления списком всех бронирований провайдера.
 *
 * @property id Уникальный идентификатор бронирования
 * @property clientName Имя клиента (snapshot, может быть анонимизировано)
 * @property startTime Время начала
 * @property endTime Время окончания
 * @property serviceName Название услуги (краткое, для отображения в списке)
 * @property status Текущий статус бронирования
 * @property totalPrice Общая стоимость бронирования
 * @property clientPhone Номер телефона клиента (опционально)
 * @property notes Заметки к бронированию (опционально)
 */
data class ProviderBooking(
    val id: String,
    val clientName: String,
    val startTime: Instant,
    val endTime: Instant,
    val serviceName: String,
    val status: BookingStatus,
    val totalPrice: Double,
    val clientPhone: String? = null,
    val notes: String? = null,
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

    /**
     * Может ли провайдер принять это бронирование.
     */
    val canAccept: Boolean
        get() = status == BookingStatus.PENDING

    /**
     * Может ли провайдер отклонить это бронирование.
     */
    val canReject: Boolean
        get() = status == BookingStatus.PENDING

    /**
     * Может ли провайдер отменить это бронирование.
     */
    val canCancel: Boolean
        get() = status.isCancellable

    companion object {
        /**
         * Creates an empty placeholder for initialization.
         */
        fun empty() = ProviderBooking(
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
 * Синхронизируется с BookingStatus в feature:booking и provider-dashboard.
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

    companion object {
        /**
         * Parse from API string value.
         */
        fun fromString(value: String): BookingStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

/**
 * Фильтр статуса бронирования для UI.
 */
enum class BookingFilter(val label: String, val status: BookingStatus?) {
    ALL("All", null),
    PENDING("Pending", BookingStatus.PENDING),
    CONFIRMED("Confirmed", BookingStatus.CONFIRMED),
    IN_PROGRESS("In Progress", BookingStatus.IN_PROGRESS),
    COMPLETED("Completed", BookingStatus.COMPLETED),
    CANCELLED("Cancelled", BookingStatus.CANCELLED);

    companion object {
        /**
         * Get filter by status.
         */
        fun fromStatus(status: BookingStatus): BookingFilter {
            return entries.find { it.status == status } ?: ALL
        }
    }
}
