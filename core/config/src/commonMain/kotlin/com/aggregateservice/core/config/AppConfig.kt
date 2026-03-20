package com.aggregateservice.core.config

expect class AppConfig(
    apiBaseUrl: String,
    apiKey: String,
    environmentCode: String = "PROD",
    languageCode: String = "ru",
    isDebug: Boolean = false,
    enableLogging: Boolean = false,
    networkTimeoutMs: Long = 30_000L,
    apiVersion: String = "v1",
    passwordMinLength: Int = 12,
    passwordMaxLength: Int = 128,
) {
    val apiBaseUrl: String
    val apiKey: String
    val environmentCode: String
    val languageCode: String
    val isDebug: Boolean
    val enableLogging: Boolean
    val networkTimeoutMs: Long
    val apiVersion: String
    val passwordMinLength: Int
    val passwordMaxLength: Int

    val environment: Environment
    val language: Language
}

enum class Environment {
    DEV,
    STAGING,
    PROD;

    companion object {
        fun fromString(value: String): Environment = when (value.uppercase()) {
            "DEV", "DEVELOPMENT" -> DEV
            "STAGING", "STAGE" -> STAGING
            else -> PROD
        }
    }
}

enum class Language(
    val code: String,
    val displayName: String,
) {
    RU("ru", "Русский"),
    HE("he", "עברית"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: RU
    }
}

object Config {
    private var _instance: AppConfig? = null

    fun initialize(config: AppConfig) {
        require(config.apiBaseUrl.isNotBlank()) { "apiBaseUrl cannot be blank" }
        require(config.networkTimeoutMs > 0) { "networkTimeoutMs must be positive" }
        require(config.apiVersion.isNotBlank()) { "apiVersion cannot be blank" }
        require(config.passwordMinLength > 0) { "passwordMinLength must be positive" }
        require(config.passwordMaxLength >= config.passwordMinLength) {
            "passwordMaxLength must be >= passwordMinLength"
        }
        _instance = config
    }

    fun reset() {
        _instance = null
    }

    val instance: AppConfig
        get() = _instance ?: throw IllegalStateException(
            "Config not initialized. Call Config.initialize() in Application.onCreate()"
        )

    val apiBaseUrl: String get() = instance.apiBaseUrl
    val apiKey: String get() = instance.apiKey
    val environment: Environment get() = instance.environment
    val language: Language get() = instance.language
    val isDebug: Boolean get() = instance.isDebug
    val enableLogging: Boolean get() = instance.enableLogging
    val networkTimeoutMs: Long get() = instance.networkTimeoutMs
    val apiVersion: String get() = instance.apiVersion
    val passwordMinLength: Int get() = instance.passwordMinLength
    val passwordMaxLength: Int get() = instance.passwordMaxLength

    val isProduction: Boolean get() = environment == Environment.PROD
    val isDevelopment: Boolean get() = environment == Environment.DEV
}
