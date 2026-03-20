package com.aggregateservice.core.i18n

/**
 * Supported application locales.
 *
 * Each locale corresponds to a language supported by the application.
 * The locale code follows BCP 47 language tag format.
 */
enum class AppLocale(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean = false,
) {
    /**
     * Russian locale.
     */
    RU(
        code = "ru",
        displayName = "Russian",
        nativeName = "Русский",
        isRtl = false,
    ),

    /**
     * Hebrew locale.
     */
    HE(
        code = "he",
        displayName = "Hebrew",
        nativeName = "עברית",
        isRtl = true,
    ),

    /**
     * English locale (default).
     */
    EN(
        code = "en",
        displayName = "English",
        nativeName = "English",
        isRtl = false,
    ),
    ;

    companion object {
        /**
         * Default locale used when no preference is set.
         */
        val DEFAULT: AppLocale = EN

        /**
         * All supported locales for display in settings.
         */
        val ALL: List<AppLocale> = entries

        /**
         * Parse locale from language code.
         *
         * @param code The BCP 47 language code (e.g., "en", "ru", "he").
         * @return The matching [AppLocale] or [DEFAULT] if not found.
         */
        public fun fromCode(code: String): AppLocale =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT

        /**
         * Parse locale from language tag (may include region).
         *
         * @param tag The BCP 47 language tag (e.g., "en-US", "ru-RU", "he-IL").
         * @return The matching [AppLocale] or [DEFAULT] if not found.
         */
        public fun fromTag(tag: String): AppLocale {
            val languageCode = tag.substringBefore("-").lowercase()
            return fromCode(languageCode)
        }
    }
}
