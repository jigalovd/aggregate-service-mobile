package com.aggregateservice.feature.catalog.domain.model

/**
 * Value Object для часов работы.
 *
 * Определяет расписание работы мастера по дням недели.
 *
 * @property monday Понедельник
 * @property tuesday Вторник
 * @property wednesday Среда
 * @property thursday Четверг
 * @property friday Пятница
 * @property saturday Суббота
 * @property sunday Воскресенье
 */
data class WorkingHours(
    val monday: DaySchedule? = null,
    val tuesday: DaySchedule? = null,
    val wednesday: DaySchedule? = null,
    val thursday: DaySchedule? = null,
    val friday: DaySchedule? = null,
    val saturday: DaySchedule? = null,
    val sunday: DaySchedule? = null,
) {
    /**
     * Получить расписание для дня недели.
     *
     * @param dayOfWeek День недели (1 = Monday, 7 = Sunday)
     * @return Расписание или null если выходной
     */
    fun getSchedule(dayOfWeek: Int): DaySchedule? = when (dayOfWeek) {
        1 -> monday
        2 -> tuesday
        3 -> wednesday
        4 -> thursday
        5 -> friday
        6 -> saturday
        7 -> sunday
        else -> null
    }

    /**
     * Проверяет, работает ли мастер в указанный день недели.
     *
     * @param dayOfWeek День недели (1 = Monday, 7 = Sunday)
     * @return true если рабочий день
     */
    fun isWorkingDay(dayOfWeek: Int): Boolean = getSchedule(dayOfWeek) != null
}

/**
 * Расписание на один день.
 *
 * @property openTime Время открытия (формат "HH:mm")
 * @property closeTime Время закрытия (формат "HH:mm")
 * @property breakStart Время начала перерыва (опционально)
 * @property breakEnd Время конца перерыва (опционально)
 */
data class DaySchedule(
    val openTime: String,
    val closeTime: String,
    val breakStart: String? = null,
    val breakEnd: String? = null,
) {
    init {
        require(openTime.matches(RegexTime)) { "openTime must be in HH:mm format" }
        require(closeTime.matches(RegexTime)) { "closeTime must be in HH:mm format" }
        breakStart?.let { require(it.matches(RegexTime)) { "breakStart must be in HH:mm format" } }
        breakEnd?.let { require(it.matches(RegexTime)) { "breakEnd must be in HH:mm format" } }
    }

    /**
     * Форматированное расписание (например, "09:00 - 18:00").
     */
    val formatted: String
        get() = if (breakStart != null && breakEnd != null) {
            "$openTime - $breakStart, $breakEnd - $closeTime"
        } else {
            "$openTime - $closeTime"
        }

    companion object {
        private val RegexTime = Regex("^([01]?[0-9]|2[0-3]):([0-5][0-9])$")
    }
}
