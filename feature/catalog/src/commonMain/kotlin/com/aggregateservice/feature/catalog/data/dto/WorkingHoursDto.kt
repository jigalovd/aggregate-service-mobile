package com.aggregateservice.feature.catalog.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для расписания работы мастера.
 *
 * **Endpoint:** GET /providers (внутри ProviderDto)
 *
 * @property monday Расписание на понедельник
 * @property tuesday Расписание на вторник
 * @property wednesday Расписание на среду
 * @property thursday Расписание на четверг
 * @property friday Расписание на пятницу
 * @property saturday Расписание на субботу
 * @property sunday Расписание на воскресенье
 */
@Serializable
data class WorkingHoursDto(
    @SerialName("monday") val monday: DayScheduleDto? = null,
    @SerialName("tuesday") val tuesday: DayScheduleDto? = null,
    @SerialName("wednesday") val wednesday: DayScheduleDto? = null,
    @SerialName("thursday") val thursday: DayScheduleDto? = null,
    @SerialName("friday") val friday: DayScheduleDto? = null,
    @SerialName("saturday") val saturday: DayScheduleDto? = null,
    @SerialName("sunday") val sunday: DayScheduleDto? = null,
)

/**
 * DTO для расписания на один день.
 *
 * @property openTime Время открытия (формат "HH:mm")
 * @property closeTime Время закрытия (формат "HH:mm")
 * @property breakStart Время начала перерыва (опционально)
 * @property breakEnd Время конца перерыва (опционально)
 */
@Serializable
data class DayScheduleDto(
    @SerialName("openTime") val openTime: String,
    @SerialName("closeTime") val closeTime: String,
    @SerialName("breakStart") val breakStart: String? = null,
    @SerialName("breakEnd") val breakEnd: String? = null,
)
