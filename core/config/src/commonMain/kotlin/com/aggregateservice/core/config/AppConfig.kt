package com.aggregateservice.core.config

/**
 * Централизованная конфигурация приложения (expect declaration)
 *
 * Реализуется platform-specific способом:
 * - Android: BuildConfig + gradle properties
 * - iOS: Info.plist / environment variables
 */
expect class AppConfig(
    apiBaseUrl: String,
    apiKey: String,
    environment: String,
    languageCode: String,
    isDebug: Boolean,
    enableLogging: Boolean,
    networkTimeoutMs: Long,
    apiVersion: String
) {
    /** Base URL для API */
    val apiBaseUrl: String

    /** API ключ для аутентификации */
    val apiKey: String

    /** Текущее окружение */
    val environment: Environment

    /** Текущий язык приложения */
    val language: Language

    /** Debug режим (включает логирование) */
    val isDebug: Boolean

    /** Включить логирование network requests */
    val enableLogging: Boolean

    /** Timeout для network requests (в миллисекундах) */
    val networkTimeoutMs: Long

    /** Версия API */
    val apiVersion: String
}

/**
 * Окружения приложения
 */
enum class Environment {
    /** Development окружение (локальная разработка) */
    DEV,

    /** Staging окружение (предпродакшн тестирование) */
    STAGING,

    /** Production окружение */
    PROD;

    companion object {
        /**
         * Определяет окружение из строки
         */
        fun fromString(value: String): Environment {
            return when (value.uppercase()) {
                "DEV", "DEVELOPMENT" -> DEV
                "STAGING", "STAGE" -> STAGING
                "PROD", "PRODUCTION" -> PROD
                else -> PROD // Default to production
            }
        }
    }
}

/**
 * Поддерживаемые языки приложения.
 *
 * @property ru Русский (дефолт)
 * @property he Иврит
 * @property en Английский
 */
enum class Language(
    val code: String,
    val displayName: String,
) {
    RU("ru", "Русский"),
    HE("he", "עברית"),
    EN("en", "English"),
    ;

    companion object {
        /**
         * Получает Language из кода языка.
         *
         * @param code Код языка (ru, he, en)
         * @return Language или RU если не найден
         */
        fun fromCode(code: String?): Language =
            entries.firstOrNull { it.code == code } ?: RU
    }
}

/**
 * Singleton для доступа к конфигурации из общего кода
 *
 * Использование:
 * ```
 * // Инициализация (в Application.onCreate для Android)
 * Config.initialize(AppConfig())
 *
 * // Использование в любом месте общего кода
 * val baseUrl = Config.apiBaseUrl
 * val apiKey = Config.apiKey
 * ```
 */
object Config {
    private var _instance: AppConfig? = null

    /**
     * Инициализация конфигурации (должна быть вызвана при старте приложения)
     *
     * @throws IllegalArgumentException если конфигурация невалидна
     */
    fun initialize(config: AppConfig) {
        // Validate configuration
        require(config.apiBaseUrl.isNotBlank()) {
            "apiBaseUrl cannot be blank. Got: '${config.apiBaseUrl}'"
        }
        require(config.apiKey.isNotBlank()) {
            "apiKey cannot be blank. Got: '${config.apiKey}'"
        }
        require(config.networkTimeoutMs > 0) {
            "networkTimeoutMs must be positive. Got: ${config.networkTimeoutMs}"
        }
        require(config.apiVersion.isNotBlank()) {
            "apiVersion cannot be blank. Got: '${config.apiVersion}'"
        }

        _instance = config
    }

    /**
     * Текущий экземпляр конфигурации
     * @throws IllegalStateException если конфигурация не инициализирована
     */
    val instance: AppConfig
        get() = _instance ?: throw IllegalStateException(
            "Config not initialized. Call Config.initialize() in Application.onCreate()"
        )

    // Convenience properties для быстрого доступа
    val apiBaseUrl: String get() = instance.apiBaseUrl
    val apiKey: String get() = instance.apiKey
    val environment: Environment get() = instance.environment
    val language: Language get() = instance.language
    val isDebug: Boolean get() = instance.isDebug
    val enableLogging: Boolean get() = instance.enableLogging
    val networkTimeoutMs: Long get() = instance.networkTimeoutMs
    val apiVersion: String get() = instance.apiVersion

    /**
     * Проверяет, является ли текущее окружение продакшном
     */
    val isProduction: Boolean
        get() = environment == Environment.PROD

    /**
     * Проверяет, является ли текущее окружение development
     */
    val isDevelopment: Boolean
        get() = environment == Environment.DEV
}
