package com.aggregateservice.feature.provider.dashboard.domain.model

/**
 * Доменная модель для отображения заработка провайдера.
 *
 * Показывает агрегированные данные по заработку
 * за разные временные периоды (сегодня, неделя, месяц).
 *
 * @property todayAmount Заработок за сегодня
 * @property weekAmount Заработок за текущую неделю
 * @property monthAmount Заработок за текущий месяц
 * @property currency Код валюты (по умолчанию ILS)
 */
data class EarningsSummary(
    val todayAmount: Double,
    val weekAmount: Double,
    val monthAmount: Double,
    val currency: String = "ILS",
) {
    /**
     * Форматированный заработок за сегодня.
     */
    val formattedToday: String
        get() = "%.0f %s".format(todayAmount, currencySymbol)

    /**
     * Форматированный заработок за неделю.
     */
    val formattedWeek: String
        get() = "%.0f %s".format(weekAmount, currencySymbol)

    /**
     * Форматированный заработок за месяц.
     */
    val formattedMonth: String
        get() = "%.0f %s".format(monthAmount, currencySymbol)

    /**
     * Символ валюты для отображения.
     */
    private val currencySymbol: String
        get() = when (currency) {
            "ILS" -> "₪"
            "USD" -> "$"
            "EUR" -> "€"
            else -> currency
        }

    companion object {
        /**
         * Пустой summary (для инициализации UI).
         */
        fun empty() = EarningsSummary(
            todayAmount = 0.0,
            weekAmount = 0.0,
            monthAmount = 0.0,
            currency = "ILS",
        )
    }
}