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
        const val CONFIRM_PASSWORD = "auth_confirm_password"
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
        const val PHONE = "auth_phone"
        const val SELECT_ROLE = "auth_select_role"
        const val CLIENT_ROLE = "auth_client_role"
        const val PROVIDER_ROLE = "auth_provider_role"
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
        const val ALL = "catalog_all"
        const val SEARCH = "catalog_search"
        const val CLEAR_FILTERS = "catalog_clear_filters"
    }

    // Search
    object Search {
        const val TITLE = "search_title"
        const val SEARCH_HINT = "search_hint"
        const val CLEAR = "search_clear"
        const val RESET_FILTERS = "search_reset_filters"
    }

    // Category
    object Category {
        const val SELECT_CATEGORY = "category_select"
        const val BACK = "category_back"
    }

    // Booking
    object Booking {
        const val TITLE = "booking_title"
        const val SELECT_SERVICE = "booking_select_service"
        const val SELECT_DATE = "booking_select_date"
        const val SELECT_TIME = "booking_select_time"
        const val CONFIRM = "booking_confirm"
        const val CONFIRM_BOOKING = "booking_confirm_booking"
        const val CANCEL = "booking_cancel"
        const val MY_BOOKINGS = "booking_my_bookings"
        const val UPCOMING = "booking_upcoming"
        const val PAST = "booking_past"
        const val NO_BOOKINGS = "booking_no_bookings"
        const val CANCEL_CONFIRMATION = "booking_cancel_confirmation"
        const val CANCEL_SUCCESS = "booking_cancel_success"
        const val BACK = "booking_back"
        const val DONE = "booking_done"
        const val SERVICES = "booking_services"
        const val DURATION = "booking_duration"
        const val NOTES = "booking_notes"
        const val NOTES_PLACEHOLDER = "booking_notes_placeholder"
        const val NO_SERVICES = "booking_no_services"
        const val CONTINUE = "booking_continue"
        const val SELECT_DATE_TIME = "booking_select_date_time"
        const val SELECT_SLOT = "booking_select_slot"
        const val SELECT_DATE_TO_SEE = "booking_select_date_to_see"
        // Booking statuses
        const val STATUS_PENDING = "booking_status_pending"
        const val STATUS_CONFIRMED = "booking_status_confirmed"
        const val STATUS_IN_PROGRESS = "booking_status_in_progress"
        const val STATUS_COMPLETED = "booking_status_completed"
        const val STATUS_CANCELLED = "booking_status_cancelled"
        const val STATUS_EXPIRED = "booking_status_expired"
        const val STATUS_NO_SHOW = "booking_status_no_show"
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
        const val REMOVE_CONFIRM = "provider_remove_confirm"
    }

    // Profile
    object Profile {
        const val TITLE = "profile_title"
        const val EDIT = "profile_edit"
        const val FULL_NAME = "profile_full_name"
        const val PHONE = "profile_phone"
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

    // Onboarding
    object Onboarding {
        const val SELECT_LANGUAGE = "onboarding_select_language"
        const val WELCOME = "onboarding_welcome"
        const val WELCOME_SUBTITLE = "onboarding_welcome_subtitle"
        const val GET_STARTED = "onboarding_get_started"
        const val ALREADY_HAVE_ACCOUNT = "onboarding_already_have_account"
        const val SIGN_IN = "onboarding_sign_in"
    }

    // Map
    object Map {
        const val NEARBY_PROVIDERS = "map_nearby_providers"
        const val YOUR_LOCATION = "map_your_location"
        const val SEARCH_THIS_AREA = "map_search_this_area"
        const val KM_AWAY = "map_km_away"
        const val SHOW_LIST = "map_show_list"
        const val SHOW_MAP = "map_show_map"
    }

    // Scheduling
    object Scheduling {
        const val SELECT_DATE = "scheduling_select_date"
        const val SELECT_TIME = "scheduling_select_time"
        const val AVAILABLE_SLOTS = "scheduling_available_slots"
        const val NO_SLOTS_AVAILABLE = "scheduling_no_slots"
        const val MORNING = "scheduling_morning"
        const val AFTERNOON = "scheduling_afternoon"
        const val EVENING = "scheduling_evening"
        const val TIMEZONE_NOTE = "scheduling_timezone_note"
    }

    // Services (Provider)
    object Services {
        const val TITLE = "services_title"
        const val MY_SERVICES = "services_my_services"
        const val ADD_SERVICE = "services_add"
        const val NO_SERVICES = "services_none"
        const val ADD_FIRST = "services_add_first"
        const val DELETE_SERVICE = "services_delete"
        const val DELETE_CONFIRM = "services_delete_confirm"
        const val SERVICE_NAME = "services_name"
        const val DESCRIPTION = "services_description"
        const val BASE_PRICE = "services_base_price"
        const val DURATION = "services_duration"
        const val DURATION_MINUTES = "services_duration_minutes"
        const val CATEGORY = "services_category"
        const val ACTIVE = "services_active"
        const val GO_BACK = "services_go_back"
        const val EDIT = "services_edit"
    }

    // Confirmation
    object Confirmation {
        const val TITLE = "confirmation_title"
        const val ALMOST_DONE = "confirmation_almost_done"
        const val REVIEW_DETAILS = "confirmation_review_details"
        const val ADD_TO_CALENDAR = "confirmation_add_to_calendar"
        const val BOOKING_CONFIRMED = "confirmation_booking_confirmed"
        const val SEE_YOU = "confirmation_see_you"
    }

    // Reviews (Phase 2)
    object Reviews {
        const val TITLE = "reviews_title"
        const val WRITE_REVIEW = "reviews_write"
        const val RATING = "reviews_rating"
        const val NO_REVIEWS = "reviews_none"
        const val HELPFUL = "reviews_helpful"
        const val REPORT = "reviews_report"
        const val BACK = "reviews_back"
        const val RETRY = "reviews_retry"
        const val CLOSE = "reviews_close"
        const val COMMENT = "reviews_comment"
        const val COMMENT_PLACEHOLDER = "reviews_comment_placeholder"
        const val CANCEL = "reviews_cancel"
        const val SUBMIT = "reviews_submit"
    }

    // Common plurals (for formatting)
    object Plurals {
        const val REVIEWS_COUNT = "plurals_reviews_count"
        const val SERVICES_COUNT = "plurals_services_count"
        const val MINUTES = "plurals_minutes"
        const val HOURS = "plurals_hours"
        const val KILOMETERS = "plurals_kilometers"
    }

    // Guest Mode prompts
    object GuestPrompt {
        const val BOOKING_TITLE = "guest_prompt_booking_title"
        const val BOOKING_MESSAGE = "guest_prompt_booking_message"
        const val REVIEW_TITLE = "guest_prompt_review_title"
        const val REVIEW_MESSAGE = "guest_prompt_review_message"
        const val FAVORITES_TITLE = "guest_prompt_favorites_title"
        const val FAVORITES_MESSAGE = "guest_prompt_favorites_message"
        const val CREATE_ACCOUNT = "guest_create_account"
        const val SIGN_IN = "guest_sign_in"
        const val MAYBE_LATER = "guest_maybe_later"
    }
}
