package com.aggregateservice.feature.booking.domain.model

/**
 * Доменная модель услуги для бронирования (Domain слой).
 *
 * **Important:** Это собственная модель feature:booking.
 * Не зависит от feature:catalog - см. [Feature Isolation Pattern](../../../docs/architecture/FEATURE_ISOLATION.md).
 *
 * **Почему своя модель вместо использования Service из catalog:**
 * - Feature Isolation: booking не должен зависеть от catalog
 * - Booking требует только данные, необходимые для бронирования
 * - Можно кэшировать независимо от catalog
 * - Разные фичи могут иметь разные требования к данным
 *
 * @property id Уникальный идентификатор услуги
 * @property name Название услуги
 * @property description Описание услуги
 * @property price Цена в минимальных единицах (агоротах/центах)
 * @property currency Код валюты (ILS, USD, RUB)
 * @property durationMinutes Длительность в минутах
 */
data class BookingService(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
) {
    /**
     * Форматированная цена (например, "150 ₪" или "150 ILS").
     */
    val formattedPrice: String
        get() = "%.0f %s".format(price, currency)

    /**
     * Форматированная длительность (например, "60 min").
     */
    val formattedDuration: String
        get() = "${durationMinutes} min"

    /**
     * Короткое описание для карточки (первые 100 символов).
     */
    val shortDescription: String?
        get() = description?.take(100)
}
