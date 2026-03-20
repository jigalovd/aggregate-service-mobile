package com.aggregateservice.core.i18n

/**
 * Interface for providing localized strings.
 *
 * Implementations should provide access to localized strings based on
 * the current locale. The provider handles loading and caching of resources.
 */
interface I18nProvider {
    /**
     * The current locale.
     */
    val currentLocale: AppLocale

    /**
     * Get a localized string by key.
     *
     * @param key The string resource key.
     * @return The localized string, or the key itself if not found.
     */
    operator fun get(key: String): String

    /**
     * Get a localized string by key with format arguments.
     *
     * @param key The string resource key.
     * @param args Format arguments to substitute into the string.
     * @return The formatted localized string.
     */
    fun get(key: String, vararg args: Any?): String

    /**
     * Get a localized string by key, returning null if not found.
     *
     * @param key The string resource key.
     * @return The localized string, or null if not found.
     */
    fun getOrNull(key: String): String?

    /**
     * Change the current locale.
     *
     * @param locale The new locale to use.
     */
    fun setLocale(locale: AppLocale)

    /**
     * Get all available locales.
     */
    fun availableLocales(): List<AppLocale> = AppLocale.ALL
}

/**
 * Extension function to get a localized string with a default value.
 *
 * @param key The string resource key.
 * @param defaultValue The default value if the key is not found.
 * @return The localized string or the default value.
 */
fun I18nProvider.getOrDefault(key: String, defaultValue: String): String =
    getOrNull(key) ?: defaultValue

/**
 * String resource keys used throughout the application.
 *
 * These keys correspond to entries in the string resource files.
 */
object StringKey {
    // Common
    const val APP_NAME = "app_name"
    const val OK = "ok"
    const val CANCEL = "cancel"
    const val SAVE = "save"
    const val DELETE = "delete"
    const val EDIT = "edit"
    const val CLOSE = "close"
    const val DONE = "done"
    const val LOADING = "loading"
    const val ERROR = "error"
    const val SUCCESS = "success"
    const val RETRY = "retry"
    const val SEARCH = "search"
    const val FILTER = "filter"
    const val SORT = "sort"
    const val CLEAR = "clear"
    const val APPLY = "apply"
    const val RESET = "reset"

    // Auth
    object Auth {
        const val LOGIN = "auth_login"
        const val LOGOUT = "auth_logout"
        const val EMAIL = "auth_email"
        const val PASSWORD = "auth_password"
        const val EMAIL_HINT = "auth_email_hint"
        const val PASSWORD_HINT = "auth_password_hint"
        const val FORGOT_PASSWORD = "auth_forgot_password"
        const val NO_ACCOUNT = "auth_no_account"
        const val SIGN_UP = "auth_sign_up"
        const val LOGIN_SUCCESS = "auth_login_success"
        const val LOGIN_ERROR = "auth_login_error"
        const val INVALID_EMAIL = "auth_invalid_email"
        const val INVALID_PASSWORD = "auth_invalid_password"
        const val SESSION_EXPIRED = "auth_session_expired"
    }

    // Navigation
    object Navigation {
        const val HOME = "nav_home"
        const val CATALOG = "nav_catalog"
        const val BOOKING = "nav_booking"
        const val FAVORITES = "nav_favorites"
        const val PROFILE = "nav_profile"
        const val SETTINGS = "nav_settings"
    }

    // Catalog
    object Catalog {
        const val TITLE = "catalog_title"
        const val SEARCH_HINT = "catalog_search_hint"
        const val FILTER_BY_CATEGORY = "catalog_filter_category"
        const val FILTER_BY_LOCATION = "catalog_filter_location"
        const val SORT_BY_RATING = "catalog_sort_rating"
        const val SORT_BY_DISTANCE = "catalog_sort_distance"
        const val NO_RESULTS = "catalog_no_results"
        const val PROVIDERS_NEARBY = "catalog_providers_nearby"
    }

    // Booking
    object Booking {
        const val TITLE = "booking_title"
        const val SELECT_SERVICE = "booking_select_service"
        const val SELECT_DATE = "booking_select_date"
        const val SELECT_TIME = "booking_select_time"
        const val CONFIRM = "booking_confirm"
        const val CANCEL = "booking_cancel"
        const val MY_BOOKINGS = "booking_my_bookings"
        const val UPCOMING = "booking_upcoming"
        const val PAST = "booking_past"
        const val NO_BOOKINGS = "booking_no_bookings"
        const val CANCEL_CONFIRMATION = "booking_cancel_confirmation"
        const val CANCEL_SUCCESS = "booking_cancel_success"
    }

    // Provider
    object Provider {
        const val SERVICES = "provider_services"
        const val REVIEWS = "provider_reviews"
        const val RATING = "provider_rating"
        const val WORKING_HOURS = "provider_working_hours"
        const val CONTACT = "provider_contact"
        const val LOCATION = "provider_location"
        const val ADD_TO_FAVORITES = "provider_add_favorites"
        const val REMOVE_FROM_FAVORITES = "provider_remove_favorites"
    }

    // Profile
    object Profile {
        const val TITLE = "profile_title"
        const val EDIT = "profile_edit"
        const val CHANGE_PASSWORD = "profile_change_password"
        const val NOTIFICATIONS = "profile_notifications"
        const val LANGUAGE = "profile_language"
        const val THEME = "profile_theme"
        const val LOGOUT = "profile_logout"
    }

    // Error messages
    object Error {
        const val NETWORK = "error_network"
        const val SERVER = "error_server"
        const val UNKNOWN = "error_unknown"
        const val VALIDATION = "error_validation"
        const val UNAUTHORIZED = "error_unauthorized"
    }
}
