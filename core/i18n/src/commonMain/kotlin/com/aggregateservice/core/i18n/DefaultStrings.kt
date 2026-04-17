package com.aggregateservice.core.i18n

/**
 * Default string resources for all supported locales.
 *
 * This file provides the default I18n provider by composing
 * per-feature string resources. For new features, add strings
 * to the appropriate feature-specific Strings object.
 *
 * Architecture Decision Record:
 *     Strings are split into per-feature objects to enable
 *     better code organization and modularity. The DefaultStrings
 *     object combines all feature strings into a single map per locale.
 */
public object DefaultStrings {
    /**
     * English strings (default).
     */
    public val EN: Map<String, String> by lazy {
        CommonStrings.EN +
            NavigationStrings.EN +
            AuthStrings.EN +
            CatalogStrings.EN +
            BookingStrings.EN +
            ProviderStrings.EN +
            ProfileStrings.EN +
            ErrorStrings.EN +
            OnboardingStrings.EN +
            MapStrings.EN +
            ReviewsStrings.EN +
            GuestStrings.EN +
            ValidationStrings.EN
    }

    /**
     * Russian strings.
     */
    public val RU: Map<String, String> by lazy {
        CommonStrings.RU +
            NavigationStrings.RU +
            AuthStrings.RU +
            CatalogStrings.RU +
            BookingStrings.RU +
            ProviderStrings.RU +
            ProfileStrings.RU +
            ErrorStrings.RU +
            OnboardingStrings.RU +
            MapStrings.RU +
            ReviewsStrings.RU +
            GuestStrings.RU +
            ValidationStrings.RU
    }

    /**
     * Hebrew strings.
     */
    public val HE: Map<String, String> by lazy {
        CommonStrings.HE +
            NavigationStrings.HE +
            AuthStrings.HE +
            CatalogStrings.HE +
            BookingStrings.HE +
            ProviderStrings.HE +
            ProfileStrings.HE +
            ErrorStrings.HE +
            OnboardingStrings.HE +
            MapStrings.HE +
            ReviewsStrings.HE +
            GuestStrings.HE +
            ValidationStrings.HE
    }

    /**
     * Get all default resources as a map.
     */
    public fun all(): Map<AppLocale, Map<String, String>> =
        mapOf(
            AppLocale.EN to EN,
            AppLocale.RU to RU,
            AppLocale.HE to HE,
        )
}

/**
 * Creates the default I18n provider with all built-in strings.
 *
 * @param initialLocale The initial locale to use.
 * @return A configured [I18nProvider] with default strings.
 */
public fun createDefaultI18nProvider(
    initialLocale: AppLocale = AppLocale.DEFAULT,
): I18nProvider =
    I18nProviderImpl(
        initialLocale = initialLocale,
        resources = DefaultStrings.all(),
    )
