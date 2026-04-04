package com.aggregateservice.core.common.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Value Object для географического местоположения.
 *
 * Используется для:
 * - Определения местоположения мастера (Provider)
 * - Геопоиска пользователей
 * - Отображения на карте
 *
 * **Важно:** Этот класс НЕ содержит никаких платформенных зависимостей.
 *
 * @property latitude Широта
 * @property longitude Долгота
 * @property address Адрес (улица, дом)
 * @property city Город
 * @property postalCode Почтовый индекс (опционально)
 * @property country Страна (опционально)
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val postalCode: String? = null,
    val country: String? = null,
) {
    /**
     * Расстояние между двумя точками в километрах (формула гаверсинуса).
     *
     * @param other Другое местоположение
     * @return Расстояние в километрах
     */
    fun distanceTo(other: Location): Double {
        val earthRadiusKm = 6371.0
        val latDistance = Math.toRadians(other.latitude - latitude)
        val lonDistance = Math.toRadians(other.longitude - longitude)
        val a =
            sin(latDistance / 2).pow(2.0) +
                cos(Math.toRadians(latitude)) *
                cos(Math.toRadians(other.latitude)) *
                sin(lonDistance / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Полный адрес для отображения.
     */
    val fullAddress: String
        get() =
            listOfNotNull(address, city, postalCode, country)
                .filter { it.isNotBlank() }
                .joinToString(", ")

    companion object {
        /**
         * Пустое местоположение (placeholder).
         */
        val Empty =
            Location(
                latitude = 0.0,
                longitude = 0.0,
                address = "",
                city = "",
            )

        /**
         * Местоположение по умолчанию (Хайфа, Израиль).
         * Используется как stub для iOS и fallback для Android.
         */
        val DEFAULT =
            Location(
                latitude = 32.8,
                longitude = 35.0,
                address = "",
                city = "Haifa",
            )
    }
}
