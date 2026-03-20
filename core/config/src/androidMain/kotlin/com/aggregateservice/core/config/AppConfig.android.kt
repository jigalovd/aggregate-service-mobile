package com.aggregateservice.core.config

/**
 * Android-специфичная реализация конфигурации
 *
 * Параметры передаются через конструктор для избежания циклической зависимости
 */
actual class AppConfig actual constructor(
    apiBaseUrl: String,
    apiKey: String,
    environment: String,
    languageCode: String,
    isDebug: Boolean,
    enableLogging: Boolean,
    networkTimeoutMs: Long,
    apiVersion: String
) {
    actual val apiBaseUrl: String = apiBaseUrl
    actual val apiKey: String = apiKey
    actual val environment: Environment = Environment.fromString(environment)
    actual val language: Language = Language.fromCode(languageCode)
    actual val isDebug: Boolean = isDebug
    actual val enableLogging: Boolean = enableLogging
    actual val networkTimeoutMs: Long = networkTimeoutMs
    actual val apiVersion: String = apiVersion

    override fun toString(): String = """
        |
        |AppConfig (Android):
        |  - API Base URL: $apiBaseUrl
        |  - API Key: ${apiKey.take(3)}*** (hidden)
        |  - Environment: $environment
        |  - Language: $language
        |  - Debug: $isDebug
        |  - Logging: $enableLogging
        |  - Network Timeout: ${networkTimeoutMs}ms
        |  - API Version: $apiVersion
        |
    """.trimMargin()
}
