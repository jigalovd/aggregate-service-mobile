package com.aggregateservice.feature.booking.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Value object для временного слота.
 *
 * Представляет доступный или занятый временной интервал
 * для бронирования услуги.
 *
 * @property startTime Начало слота
 * @property endTime Конец слота
 * @property isAvailable Доступен ли слот для бронирования
 * @property providerId ID мастера, к которому относится слот
 */
data class TimeSlot(
    val startTime: Instant,
    val endTime: Instant,
    val isAvailable: Boolean,
    val providerId: String,
) {
    /**
     * Длительность слота в минутах.
     */
    val durationMinutes: Int
        get() = ((endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()) / 60000).toInt()

    /**
     * Форматированное время начала (например, "09:00").
     */
    val formattedStartTime: String
        get() = startTime.toLocalTimeString()

    /**
     * Форматированное время окончания (например, "10:00").
     */
    val formattedEndTime: String
        get() = endTime.toLocalTimeString()

    /**
     * Форматированный диапазон времени (например, "09:00 - 10:00").
     */
    val formattedTimeRange: String
        get() = "$formattedStartTime - $formattedEndTime"

    companion object {
        /**
         * Создаёт пустой (недоступный) слот.
         */
        fun empty(providerId: String) = TimeSlot(
            startTime = Instant.fromEpochMilliseconds(0),
            endTime = Instant.fromEpochMilliseconds(0),
            isAvailable = false,
            providerId = providerId,
        )
    }
}

/**
 * Extension function для форматирования Instant в LocalTime string.
 */
private fun Instant.toLocalTimeString(): String {
    val localDateTime = toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}
