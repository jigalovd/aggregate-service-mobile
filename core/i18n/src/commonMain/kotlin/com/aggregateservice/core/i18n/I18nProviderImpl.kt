package com.aggregateservice.core.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default implementation of [I18nProvider].
 *
 * Provides localized strings based on the current locale using
 * a map-based resource bundle approach.
 */
class I18nProviderImpl(
    initialLocale: AppLocale = AppLocale.DEFAULT,
    private val resources: Map<AppLocale, Map<String, String>> = emptyMap(),
) : I18nProvider {
    private val _localeFlow = MutableStateFlow(initialLocale)
    public val localeFlow: StateFlow<AppLocale> = _localeFlow.asStateFlow()

    override var currentLocale: AppLocale by _localeFlow::value

    override fun get(key: String): String =
        getOrNull(key) ?: key

    override fun get(key: String, vararg args: Any?): String {
        val template = get(key)
        return if (args.isEmpty()) {
            template
        } else {
            try {
                template.format(*args)
            } catch (e: Exception) {
                template
            }
        }
    }

    override fun getOrNull(key: String): String? =
        resources[currentLocale]?.get(key)

    override fun setLocale(locale: AppLocale) {
        _localeFlow.value = locale
    }
}

/**
 * Creates an [I18nProvider] with the given locale and resources.
 *
 * @param locale The initial locale.
 * @param block A builder block to add string resources.
 * @return A configured [I18nProvider].
 */
public fun i18nProvider(
    locale: AppLocale = AppLocale.DEFAULT,
    block: I18nBuilder.() -> Unit,
): I18nProvider {
    val builder = I18nBuilder()
    builder.block()
    return I18nProviderImpl(locale, builder.build())
}

/**
 * Builder for creating string resource maps.
 */
public class I18nBuilder {
    private val resources: MutableMap<AppLocale, MutableMap<String, String>> = mutableMapOf()

    /**
     * Add strings for a specific locale.
     */
    public fun locale(locale: AppLocale, strings: Map<String, String>) {
        resources.getOrPut(locale) { mutableMapOf() }.putAll(strings)
    }

    /**
     * Add strings for Russian locale.
     */
    public fun ru(strings: Map<String, String>) {
        locale(AppLocale.RU, strings)
    }

    /**
     * Add strings for Hebrew locale.
     */
    public fun he(strings: Map<String, String>) {
        locale(AppLocale.HE, strings)
    }

    /**
     * Add strings for English locale.
     */
    public fun en(strings: Map<String, String>) {
        locale(AppLocale.EN, strings)
    }

    public fun build(): Map<AppLocale, Map<String, String>> =
        resources.mapValues { it.value.toMap() }
}
