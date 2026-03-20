package com.aggregateservice.core.utils

import com.aggregateservice.core.config.Config
import com.aggregateservice.core.config.Language

/**
 * i18n (internationalization) extensions для локализации полей.
 *
 * **Backend Pattern:**
 * Бэкенд возвращает локализованные поля в формате `_i18n`:
 * ```json
 * {
 *   "title_i18n": {
 *     "ru": "Стрижка",
 *     "he": "תספורת",
 *     "en": "Haircut"
 *   }
 * }
 * ```
 *
 * **This Helper:**
 * - Flattens `_i18n` Map to single String based on user's language
 * - Fallback logic: current language → first available → default value
 * - Type-safe extensions for common use cases
 *
 * **Usage:**
 * ```kotlin
 * val title = serviceDto.title_i18n.localize()  // Returns "Стрижка" if language=ru
 * val name = categoryDto.nameI18n.localizeOrDefault("Unknown Category")
 * ```
 *
 * @see BACKEND_API_REFERENCE.md секция 8 "Локализация"
 */

/**
 * Get current language from Config.
 *
 * **Priority:**
 * 1. Config.language.code (from user preferences)
 * 2. System language (if supported)
 * 3. RU (default)
 */
fun getCurrentLanguage(): Language =
    Language.fromCode(
        try {
            Config.language.code
        } catch (e: Exception) {
            null
        },
    )

/**
 * Localize a Map of language strings to user's current language.
 *
 * **Fallback Logic:**
 * 1. Try to get value for current language
 * 2. If not found, get first available value
 * 3. If map is empty, return null
 *
 * **Usage:**
 * ```kotlin
 * val title = mapOf("ru" to "Стрижка", "he" to "תספורת").localize()
 * // Returns "Стрижка" if current language is RU
 * ```
 *
 * @return Localized string or null if map is empty
 */
fun Map<String, String>.localize(): String? {
    val currentLanguage = getCurrentLanguage()

    // Try current language first
    this[currentLanguage.code]?.let { return it }

    // Fallback to first available value
    return values.firstOrNull()
}

/**
 * Localize a Map with default fallback value.
 *
 * **Usage:**
 * ```kotlin
 * val title = mapOf("ru" to "Стрижка").localizeOrDefault("Untitled")
 * // Returns "Стрижка" if language=ru, "Untitled" if map is empty
 * ```
 *
 * @param defaultValue Default value if localization fails
 * @return Localized string or default value
 */
fun Map<String, String>.localizeOrDefault(defaultValue: String): String =
    localize() ?: defaultValue

/**
 * Extension for nullable Map localization.
 *
 * **Usage:**
 * ```kotlin
 * val title: Map<String, String>? = serviceDto.title_i18n
 * val localized = title.localizeOrNull()  // Returns null if map is null
 * ```
 *
 * @return Localized string or null if map is null/empty
 */
fun Map<String, String>?.localizeOrNull(): String? = this?.localize()

/**
 * Extension for nullable Map with default fallback.
 *
 * **Usage:**
 * ```kotlin
 * val title: Map<String, String>? = serviceDto.title_i18n
 * val localized = title.localizeOrNullOrDefault("Untitled")  // "Untitled" if null
 * ```
 *
 * @param defaultValue Default value if map is null or localization fails
 * @return Localized string or default value
 */
fun Map<String, String>?.localizeOrNullOrDefault(defaultValue: String): String =
    this?.localizeOrDefault(defaultValue) ?: defaultValue

/**
 * Check if Map contains current language.
 *
 * **Usage:**
 * ```kotlin
 * val isLocalized = mapOf("ru" to "Стрижка").hasCurrentLanguage()
 * // Returns true if current language is RU
 * ```
 *
 * @return true if map contains value for current language
 */
fun Map<String, String>.hasCurrentLanguage(): Boolean =
    getCurrentLanguage().code in this.keys

/**
 * Get all available languages from Map.
 *
 * **Usage:**
 * ```kotlin
 * val languages = mapOf("ru" to "A", "en" to "B").getAvailableLanguages()
 * // Returns [Language.RU, Language.EN]
 * ```
 *
 * @return List of available languages
 */
fun Map<String, String>.getAvailableLanguages(): List<Language> =
    keys.mapNotNull { code ->
        Language.entries.firstOrNull { it.code == code }
    }

/**
 * DTOs with i18n fields extensions.
 *
 * These extensions provide property-like access for common DTO fields.
 */

/**
 * Extension to localize title_i18n field.
 */
val Map<String, String>?.localizedTitle: String
    get() = this.localizeOrNullOrDefault("Untitled")

/**
 * Extension to localize name_i18n field.
 */
val Map<String, String>?.localizedName: String
    get() = this.localizeOrNullOrDefault("Unnamed")

/**
 * Extension to localize description_i18n field.
 */
val Map<String, String>?.localizedDescription: String?
    get() = this.localizeOrNull()

/**
 * Extension to localize bio_i18n field.
 */
val Map<String, String>?.localizedBio: String?
    get() = this.localizeOrNull()

/**
 * Validate if i18n Map contains all required languages.
 *
 * **Usage:**
 * ```kotlin
 * val isValid = mapOf("ru" to "A", "he" to "B").hasAllLanguages()
 * // Returns true if all required languages are present
 * ```
 *
 * @param requiredLanguages Required languages (default: RU, HE, EN)
 * @return true if all required languages are present
 */
fun Map<String, String>.hasAllLanguages(
    requiredLanguages: List<Language> = Language.entries,
): Boolean = requiredLanguages.all { it.code in this.keys }

/**
 * Get missing languages from i18n Map.
 *
 * **Usage:**
 * ```kotlin
 * val missing = mapOf("ru" to "A").getMissingLanguages()
 * // Returns [Language.HE, Language.EN]
 * ```
 *
 * @param requiredLanguages Required languages (default: RU, HE, EN)
 * @return List of missing languages
 */
fun Map<String, String>.getMissingLanguages(
    requiredLanguages: List<Language> = Language.entries,
): List<Language> = requiredLanguages.filter { it.code !in this.keys }

/**
 * Format a localized string with arguments.
 *
 * **Usage:**
 * ```kotlin
 * val template = mapOf("ru" to "Здравствуйте, %s!", "en" to "Hello, %s!")
 * val formatted = template.localizeFormat("John")
 * // Returns "Здравствуйте, John!" if language=ru
 * ```
 *
 * **Note:** This is a simple KMP-safe replacement for String.format.
 * For advanced formatting needs, consider using ICU4K library.
 *
 * @param args Arguments to format into the string
 * @return Formatted localized string
 */
fun Map<String, String>.localizeFormat(
    vararg args: Any,
): String {
    val template = localize() ?: return ""

    // KMP-safe string formatting without JVM dependencies
    return args.fold(template) { acc, arg ->
        // Replace first occurrence of %s with argument
        val placeholderIndex = acc.indexOf("%s")
        if (placeholderIndex != -1) {
            acc.substring(0, placeholderIndex) +
            arg.toString() +
            acc.substring(placeholderIndex + 2)
        } else {
            acc
        }
    }
}
