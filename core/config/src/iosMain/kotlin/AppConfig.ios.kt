package com.aggregateservice.core.config

import platform.Foundation.NSUserDefaults

/**
 * iOS реализация [AppConfig].
 *
 * Получает параметры из Info.plist или NSUserDefaults.
 */
actual data class AppConfig(
    actual val apiBaseUrl: String,
    actual val apiKey: String,
    private val environmentString: String,
    private val languageCode: String,
    actual val isDebug: Boolean,
    actual val enableLogging: Boolean,
    actual val networkTimeoutMs: Long,
    actual val apiVersion: String,
) {
    actual val environment: Environment = Environment.fromString(environmentString)
    actual val language: Language = Language.fromCode(languageCode)

    companion object {
        /**
         * Создает AppConfig из Info.plist.
         */
        fun loadFromInfoPlist(): AppConfig {
            val userDefaults = NSUserDefaults.standardUserDefaults

            return AppConfig(
                apiBaseUrl = userDefaults.stringForKey("API_BASE_URL") ?: "",
                apiKey = userDefaults.stringForKey("API_KEY") ?: "",
                environmentString = userDefaults.stringForKey("ENVIRONMENT") ?: "prod",
                languageCode = userDefaults.stringForKey("LANGUAGE") ?: "ru",
                isDebug = userDefaults.boolForKey("IS_DEBUG"),
                enableLogging = userDefaults.boolForKey("ENABLE_LOGGING"),
                networkTimeoutMs = (userDefaults.objectForKey("NETWORK_TIMEOUT") as? Number)?.toLong() ?: 30_000L,
                apiVersion = userDefaults.stringForKey("API_VERSION") ?: "v1",
            )
        }
    }
}

