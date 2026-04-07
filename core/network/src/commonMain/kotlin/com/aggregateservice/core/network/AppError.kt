package com.aggregateservice.core.network

/**
 * Sealed hierarchy для всех ошибок приложения.
 *
 * Обеспечивает type-safe обработку ошибок от API и других источников.
 * Согласован с [BACKEND_API_REFERENCE.md](https://github.com/your-repo/docs/BACKEND_API_REFERENCE.md).
 *
 * @see BACKEND_API_REFERENCE.md секция 6 "Статусы и коды ответов"
 */
sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause) {
    /**
     * Network ошибки (4xx, 5xx)
     *
     * @property code HTTP код ответа
     * @property message Сообщение об ошибке (из response body)
     * @property detail Детальная информация (опционально)
     */
    data class NetworkError(
        val code: Int,
        override val message: String,
        val detail: String? = null,
    ) : AppError(message)

    /**
     * Ошибка авторизации (401)
     *
     * Используется когда токен отсутствует, недействителен или истек.
     */
    data object Unauthorized : AppError("Unauthorized access - token missing or invalid") {
        private fun readResolve(): Any = Unauthorized
    }

    /**
     * Аккаунт заблокирован (423 Locked)
     *
     * Бэкенд блокирует аккаунт после 5 неудачных попыток входа на 15 минут.
     *
     * @property until ISO 8601 datetime до которого заблокирован аккаунт
     *
     * @see BACKEND_API_REFERENCE.md секция 3.5 "Account Lockout"
     */
    data class AccountLocked(
        val until: String,
    ) : AppError("Account locked until $until")

    /**
     * Ошибка валидации (422 Unprocessable Entity)
     *
     * Бэкенд возвращает детальную информацию о невалидных полях.
     *
     * @property field Имя поля которое не прошло валидацию
     * @property message Сообщение об ошибке
     *
     * @see BACKEND_API_REFERENCE.md секция 6.2 "Error Response Format"
     */
    data class ValidationError(
        val field: String,
        override val message: String,
    ) : AppError(message)

    /**
     * Слот для бронирования недоступен (409 Conflict)
     *
     * Возникает при попытке создать бронирование на занятой слот.
     *
     * @property reason Причина почему слот недоступен
     */
    data class SlotNotAvailable(
        val reason: String,
    ) : AppError(reason)

    /**
     * Превышен лимит запросов (429 Too Many Requests)
     *
     * Бэкенд имеет rate limiting для разных эндпоинтов.
     *
     * @property retryAfter Количество секунд до следующей попытки
     *
     * @see BACKEND_API_REFERENCE.md секция 9 "Rate Limiting"
     */
    data class RateLimitExceeded(
        val retryAfter: Int,
    ) : AppError("Rate limit exceeded. Retry after $retryAfter seconds")

    /**
     * Ресурс не найден (404 Not Found)
     *
     * Используется когда запрошенный ресурс не существует.
     */
    data object NotFound : AppError("Resource not found") {
        private fun readResolve(): Any = NotFound
    }

    /**
     * Доступ запрещен (403 Forbidden)
     *
     * Используется когда у пользователя нет прав для выполнения операции.
     *
     * @property reason Причина запрета (опционально)
     */
    data class Forbidden(
        override val message: String? = null,
    ) : AppError(message)

    /**
     * Конфликт (409 Conflict)
     *
     * Используется для различных конфликтов (уже существует, слот занят, и т.д.).
     *
     * @property reason Причина конфликта
     */
    data class Conflict(
        override val message: String,
    ) : AppError(message)

    /**
     * Неизвестная ошибка
     *
     * Используется для всех остальных случаев (network errors, parsing errors, etc.).
     *
     * @property throwable Оригинальное исключение
     * @property message Сообщение об ошибке
     */
    data class UnknownError(
        val throwable: Throwable? = null,
        override val message: String? = null,
    ) : AppError(message, throwable)

    /**
     * Требуется связывание Firebase аккаунта с существующим аккаунтом (409 Conflict)
     *
     * Возникает при попытке войти через Firebase, когда Firebase аккаунт
     * не связан с существующим аккаунтом в системе.
     *
     * @property firebaseToken Firebase token для завершения связывания
     * @property email Email существующего аккаунта
     * @property firebaseUid Firebase UID
     * @property provider Auth provider (google, apple, phone)
     * @property message Сообщение об ошибке
     *
     * @see BACKEND_API_REFERENCE.md секция "Firebase Authentication"
     */
    data class FirebaseLinkRequired(
        val firebaseToken: String,
        val email: String,
        val firebaseUid: String,
        val provider: String,
        override val message: String,
    ) : AppError(message)
}

/**
 * Преобразует HTTP код в соответствующий [AppError].
 *
 * @param code HTTP код ответа
 * @param message Сообщение из response body
 * @return Соответствующий [AppError]
 */
fun httpCodeToAppError(
    code: Int,
    message: String = "",
): AppError =
    when (code) {
        400 -> AppError.NetworkError(code, message)
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden(message)
        404 -> AppError.NotFound
        409 -> AppError.Conflict(message)
        422 -> AppError.ValidationError("unknown", message)
        423 -> AppError.AccountLocked(message)
        429 -> AppError.RateLimitExceeded(NetworkConstants.DEFAULT_RETRY_AFTER_SECONDS)
        in 400..499 -> AppError.NetworkError(code, message)
        in 500..599 -> AppError.NetworkError(code, message)
        else -> AppError.UnknownError(message = "Unexpected HTTP code: $code")
    }

/**
 * Extension функция для преобразования [Throwable] в [AppError].
 *
 * Используется для консистентной обработки ошибок в ScreenModel и Repository.
 *
 * @return [AppError] соответствующий типу исключения
 */
fun Throwable.toAppError(): AppError =
    when (this) {
        is AppError -> this
        else -> AppError.UnknownError(
            throwable = this,
            message = this.message,
        )
    }
