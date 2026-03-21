package com.aggregateservice.feature.catalog.domain.model

import kotlinx.datetime.Instant

/**
 * Чистая доменная модель услуги (Service).
 *
 * Service - это конкретная услуга, предоставляемая мастером (Provider).
 * Каждая услуга имеет цену, длительность и принадлежит к категории.
 *
 * **Important:** Этот класс НЕ содержит никаких платформенных зависимостей
 * или DTO из network слоя. Только чистые бизнес-данные.
 *
 * @property id Уникальный идентификатор услуги
 * @property providerId ID мастера
 * @property categoryId ID категории
 * @property name Название услуги (i18n)
 * @property description Описание услуги (i18n)
 * @property price Цена услуги
 * @property durationMinutes Длительность в минутах
 * @property isActive Активна ли услуга
 * @property createdAt Дата создания услуги
 */
data class Service(
    val id: String,
    val providerId: String,
    val categoryId: String,
    val name: String,
    val description: String? = null,
    val price: Price,
    val durationMinutes: Int,
    val isActive: Boolean = true,
    val createdAt: kotlinx.datetime.Instant,
) {
    /**
     * Форматированная длительность (например, "60 min").
     */
    val formattedDuration: String
        get() = "${durationMinutes} min"

    /**
     * Короткое описание для карточки (первые 100 символов description).
     */
    val shortDescription: String?
        get() = description?.take(100)
}

/**
 * Value Object для цены услуги.
 *
 * Использует Double для совместимости с Kotlin Multiplatform.
 * Для точных расчётов рекомендуется конвертировать в минимальные единицы валюты.
 *
 * @property amount Сумма (например, 150.0 для 150 шекелей)
 * @property currency Код валюты (например, "ILS", "USD", "RUB")
 */
data class Price(
    val amount: Double,
    val currency: String,
) {
    /**
     * Форматированная цена (например, "150 ₪" или "150 ILS").
     */
    val formatted: String
        get() = "%.0f %s".format(amount, currency)

    /**
     * Сумма в минимальных единицах (копейках/центах).
     * Например, 150.50 ILS -> 15050 агорот.
     */
    val amountInCents: Long
        get() = (amount * 100).toLong()
}
