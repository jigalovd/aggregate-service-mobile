package com.aggregateservice.core.config

actual class AppConfig actual constructor(
    actual val apiBaseUrl: String,
    actual val apiKey: String,
    actual val environmentCode: String,
    actual val languageCode: String,
    actual val isDebug: Boolean,
    actual val enableLogging: Boolean,
    actual val networkTimeoutMs: Long,
    actual val apiVersion: String,
    actual val passwordMinLength: Int,
    actual val passwordMaxLength: Int,
) {
    actual val environment: Environment = Environment.fromString(environmentCode)
    actual val language: Language = Language.fromCode(languageCode)

    override fun toString(): String = """
        |AppConfig (Android):
        |  - API Base URL: $apiBaseUrl
        |  - API Key: ${apiKey.take(3)}*** (hidden)
        |  - Environment: $environment
        |  - Language: $language
        |  - Debug: $isDebug
        |  - Logging: $enableLogging
        |  - Network Timeout: ${networkTimeoutMs}ms
        |  - API Version: $apiVersion
        |  - Password Min Length: $passwordMinLength
        |  - Password Max Length: $passwordMaxLength
        |
    """.trimMargin()
}
