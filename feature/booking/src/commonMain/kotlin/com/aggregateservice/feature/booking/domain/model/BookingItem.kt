package com.aggregateservice.feature.booking.domain.model

/**
 * Позиция в бронировании (услуга + цена + длительность).
 *
 * Представляет одну услугу в составе бронирования.
 * Цена и длительность фиксируются на момент создания бронирования.
 *
 * @property id Уникальный идентификатор позиции
 * @property serviceId ID услуги из каталога
 * @property serviceName Название услуги (snapshot на момент бронирования)
 * @property price Цена услуги (snapshot на момент бронирования)
 * @property durationMinutes Длительность услуги в минутах
 */
data class BookingItem(
    val id: String,
    val serviceId: String,
    val serviceName: String,
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
}
