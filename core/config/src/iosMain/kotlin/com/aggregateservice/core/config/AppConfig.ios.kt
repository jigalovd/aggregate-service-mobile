package com.aggregateservice.core.config

import platform.Foundation.NSBundle
import platform.darwin.getenv

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
    actual override val environment: Environment = Environment.fromString(environmentCode)
    actual override val language: Language = Language.fromCode(languageCode)

    override fun toString(): String = """
        |
        |AppConfig (iOS):
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

    companion object {
        private fun readConfigKey(
            envKey: String,
            plistKey: String,
            defaultValue: String
        ): String {
            val envValue = getenv(envKey)?.toKString()
            if (!envValue.isNullOrBlank()) {
                return envValue
            }

            val plistValue = NSBundle.mainBundle.objectForInfoDictionaryKey(plistKey) as? String
            if (!plistValue.isNullOrBlank()) {
                return plistValue
            }

            return defaultValue
        }
    }
}
