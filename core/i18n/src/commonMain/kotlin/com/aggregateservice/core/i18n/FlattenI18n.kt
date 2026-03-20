package com.aggregateservice.core.i18n

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Helper object for flattening _i18n fields from API responses.
 *
 * The backend returns localized fields in the format:
 * ```json
 * {
 *   "name": {
 *     "_i18n": {
 *       "en": "Service",
 *       "ru": "Услуга",
 *       "he": "שירות"
 *     }
 *   }
 * }
 * ```
 *
 * This helper extracts the appropriate language value based on the current locale.
 */
public object FlattenI18n {

    /**
     * Extract a localized string from an _i18n field.
     *
     * @param i18nObject The JSON object containing the _i18n field.
     * @param locale The target locale.
     * @return The localized string, or null if not found.
     */
    public fun extractString(
        i18nObject: JsonObject,
        locale: AppLocale,
    ): String? {
        val i18n = i18nObject.jsonObject["_i18n"]?.jsonObject ?: return null
        return i18n[locale.code]?.jsonPrimitive?.content
    }

    /**
     * Extract a localized string with fallback.
     *
     * @param i18nObject The JSON object containing the _i18n field.
     * @param locale The target locale.
     * @param fallbackLocale The fallback locale if the primary is not found.
     * @return The localized string, or null if neither locale is found.
     */
    public fun extractString(
        i18nObject: JsonObject,
        locale: AppLocale,
        fallbackLocale: AppLocale = AppLocale.DEFAULT,
    ): String? {
        return extractString(i18nObject, locale)
            ?: extractString(i18nObject, fallbackLocale)
    }

    /**
     * Extract a localized string from a map representation.
     *
     * @param map The map containing the _i18n field.
     * @param locale The target locale.
     * @return The localized string, or null if not found.
     */
    @Suppress("UNCHECKED_CAST")
    public fun extractStringFromMap(
        map: Map<String, Any?>,
        locale: AppLocale,
    ): String? {
        val i18n = (map["_i18n"] as? Map<String, Any?>) ?: return null
        return i18n[locale.code] as? String
    }

    /**
     * Extract a localized string from a map with fallback.
     *
     * @param map The map containing the _i18n field.
     * @param locale The target locale.
     * @param fallbackLocale The fallback locale if the primary is not found.
     * @return The localized string, or null if neither locale is found.
     */
    public fun extractStringFromMap(
        map: Map<String, Any?>,
        locale: AppLocale,
        fallbackLocale: AppLocale = AppLocale.DEFAULT,
    ): String? {
        return extractStringFromMap(map, locale)
            ?: extractStringFromMap(map, fallbackLocale)
    }
}

/**
 * Extension function to extract a localized string from an _i18n JsonObject.
 *
 * @param locale The target locale.
 * @return The localized string, or null if not found.
 */
public fun JsonObject.extractI18nString(locale: AppLocale): String? =
    FlattenI18n.extractString(this, locale)

/**
 * Extension function to extract a localized string from an _i18n map.
 *
 * @param locale The target locale.
 * @return The localized string, or null if not found.
 */
public fun Map<String, Any?>.extractI18nString(locale: AppLocale): String? =
    FlattenI18n.extractStringFromMap(this, locale)

/**
 * Data class for a localized string with all translations.
 *
 * Useful when you need to store all translations and select later.
 */
public data class LocalizedString(
    val en: String? = null,
    val ru: String? = null,
    val he: String? = null,
) {
    /**
     * Get the string for the specified locale.
     */
    public fun get(locale: AppLocale): String? = when (locale) {
        AppLocale.EN -> en
        AppLocale.RU -> ru
        AppLocale.HE -> he
    }

    /**
     * Get the string for the specified locale with fallback.
     */
    public fun get(locale: AppLocale, fallback: AppLocale): String? =
        get(locale) ?: get(fallback)

    /**
     * Get the first available string from the available translations.
     */
    public fun getFirstAvailable(): String? = en ?: ru ?: he

    public companion object {
        /**
         * Create a LocalizedString from an _i18n map.
         */
        @Suppress("UNCHECKED_CAST")
        public fun fromI18nMap(map: Map<String, Any?>): LocalizedString {
            val i18n = map["_i18n"] as? Map<String, Any?> ?: return LocalizedString()
            return LocalizedString(
                en = i18n["en"] as? String,
                ru = i18n["ru"] as? String,
                he = i18n["he"] as? String,
            )
        }

        /**
         * Create a LocalizedString from a simple string (non-localized).
         */
        public fun fromString(value: String): LocalizedString =
            LocalizedString(en = value, ru = value, he = value)
    }
}
