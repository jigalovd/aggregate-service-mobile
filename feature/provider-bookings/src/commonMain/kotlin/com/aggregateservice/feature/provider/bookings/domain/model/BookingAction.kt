package com.aggregateservice.feature.provider.bookings.domain.model

/**
 * Действия, доступные провайдеру для управления бронированиями.
 *
 * @property label Локализованная метка для отображения в UI
 */
enum class BookingAction(val label: String) {
    /**
     * Принять бронирование (изменить статус на CONFIRMED).
     */
    ACCEPT("Accept"),

    /**
     * Отклонить бронирование (изменить статус на CANCELLED с причиной).
     */
    REJECT("Reject"),

    /**
     * Отменить бронирование (изменить статус на CANCELLED).
     */
    CANCEL("Cancel");

    companion object {
        /**
         * Get action by name (case-insensitive).
         */
        fun fromString(value: String): BookingAction {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CANCEL
        }
    }
}

/**
 * Результат выполнения действия над бронированием.
 */
sealed class BookingActionResult {
    data class Success(val bookingId: String, val action: BookingAction) : BookingActionResult()

    data class Error(val bookingId: String, val action: BookingAction, val message: String) : BookingActionResult()
}
