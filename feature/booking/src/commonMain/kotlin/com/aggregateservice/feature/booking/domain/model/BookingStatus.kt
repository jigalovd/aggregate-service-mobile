package com.aggregateservice.feature.booking.domain.model

/**
 * Статусы бронирования.
 *
 * Соответствуют backend enum в app.bookings.status:
 * - PENDING: Ожидает подтверждения мастера
 * - CONFIRMED: Подтверждено мастером
 * - IN_PROGRESS: В процессе выполнения
 * - COMPLETED: Услуга оказана
 * - CANCELLED: Отменено клиентом или мастером
 * - EXPIRED: Истекло (автоматически через 24 часа без подтверждения)
 * - NO_SHOW: Клиент не явился
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    EXPIRED,
    NO_SHOW,
    ;

    /**
     * Можно ли отменить бронирование в этом статусе.
     */
    val isCancellable: Boolean
        get() = this == PENDING || this == CONFIRMED

    /**
     * Можно ли перенести бронирование в этом статусе.
     */
    val isReschedulable: Boolean
        get() = this == PENDING || this == CONFIRMED

    /**
     * Является ли бронирование активным (не завершено/не отменено).
     */
    val isActive: Boolean
        get() = this == PENDING || this == CONFIRMED || this == IN_PROGRESS

    /**
     * Является ли бронирование прошедшим.
     */
    val isPast: Boolean
        get() = this == COMPLETED || this == CANCELLED || this == EXPIRED || this == NO_SHOW
}
