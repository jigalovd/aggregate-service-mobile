package com.aggregateservice.core.config

import platform.Foundation.NSBundle
import platform.darwin.getenv

/**
 * iOS-специфичная реализация конфигурации
 *
 * Приоритет чтения конфигурации:
 * 1. Environment variables (для CI/CD)
 * 2. Info.plist (для production builds)
 * 3. Defaults (fallback)
 */
actual class AppConfig actual constructor(
    apiBaseUrl: String,
    apiKey: String,
    environment: String,
    isDebug: Boolean,
    enableLogging: Boolean,
    networkTimeoutMs: Long,
    apiVersion: String
) {
    actual val apiBaseUrl: String = apiBaseUrl
    actual val apiKey: String = apiKey
    actual val environment: Environment = Environment.fromString(environment)
    actual val isDebug: Boolean = isDebug
    actual val enableLogging: Boolean = enableLogging
    actual val networkTimeoutMs: Long = networkTimeoutMs
    actual val apiVersion: String = apiVersion

    override fun toString(): String = """
        |
        |AppConfig (iOS):
        |  - API Base URL: $apiBaseUrl
        |  - API Key: ${apiKey.take(3)}*** (hidden)
        |  - Environment: $environment
        |  - Debug: $isDebug
        |  - Logging: $enableLogging
        |  - Network Timeout: ${networkTimeoutMs}ms
        |  - API Version: $apiVersion
        |
    """.trimMargin()

    companion object {
        /**
         * Читает конфигурацию с приоритетом:
         * 1. Environment variable
         * 2. Info.plist
         * 3. Default value
         */
        private fun readConfigKey(
            envKey: String,
            plistKey: String,
            defaultValue: String
        ): String {
            // 1. Попытка чтения из environment variable
            val envValue = getenv(envKey)?.toKString()
            if (!envValue.isNullOrBlank()) {
                return envValue
            }

            // 2. Попытка чтения из Info.plist
            val plistValue = NSBundle.mainBundle.objectForInfoDictionaryKey(plistKey) as? String
            if (!plistValue.isNullOrBlank()) {
                return plistValue
            }

            // 3. Fallback to default
            return defaultValue
        }
    }
}
