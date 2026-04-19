package com.aggregateservice.core.config

/**
 * JVM actual implementation of [AppConfig].
 *
 * For unit tests, create instances directly:
 * ```kotlin
 * val config = AppConfig(
 *     apiBaseUrl = "https://api.example.com",
 *     apiKey = "test-key",
 *     environmentCode = "DEV",
 *     languageCode = "ru",
 *     isDebug = true,
 *     enableLogging = false,
 *     networkTimeoutMs = 30_000L,
 *     apiVersion = "v1",
 *     passwordMinLength = 12,
 *     passwordMaxLength = 128,
 * )
 * ```
 *
 * Or use [Config.initialize] then [Config.instance].
 */
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
}