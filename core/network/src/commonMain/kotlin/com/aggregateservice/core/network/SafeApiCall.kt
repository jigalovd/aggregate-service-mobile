package com.aggregateservice.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

/**
 * Safe API call wrapper с автоматической обработкой ошибок и retry логикой.
 *
 * **Функции:**
 * - Обработка всех HTTP кодов (200, 201, 204, 400, 401, 403, 404, 409, 422, 423, 429, 500)
 * - Автоматический retry при 500 (max 3 попытки)
 * - Rate limiting обработка (X-RateLimit-* headers)
 * - Парсинг ошибок из response body
 *
 * **Использование:**
 * ```kotlin
 * val result = safeApiCall {
 *     httpClient.post("auth/login") {
 *         setBody(LoginRequest(email, password))
 *     }
 * }
 * when (result) {
 *     is Result.Success -> println(result.data)
 *     is Result.Error -> handleError(result.error)
 * }
 * ```
 *
 * @see BACKEND_API_REFERENCE.md секция 6 "Статусы и коды ответов"
 * @see AppError
 */
suspend inline fun <reified T : Any> safeApiCall(
    maxRetries: Int = NetworkConstants.MAX_RETRIES,
    retryDelayMs: Long = NetworkConstants.RETRY_DELAY_MS,
    crossinline apiCall: suspend () -> HttpResponse,
): Result<T> {
    var lastException: Throwable? = null

    // Retry loop для 500 ошибок
    repeat(maxRetries) { attempt ->
        try {
            val response = apiCall()

            // Проверяем HTTP статус
            return when (response.status) {
                // Success responses
                HttpStatusCode.OK,
                HttpStatusCode.Created,
                HttpStatusCode.Accepted,
                -> {
                    val data = response.body<T>()
                    Result.success(data)
                }

                // No Content (204)
                HttpStatusCode.NoContent -> {
                    @Suppress("UNCHECKED_CAST")
                    val result =
                        if (null is T) {
                            Result.success(null as T)
                        } else {
                            Result.success(Unit as T)
                        }
                    result
                }

                // Rate Limit Exceeded (429)
                HttpStatusCode.TooManyRequests -> {
                    val retryAfter = parseRetryAfter(response)
                    Result.failure(
                        AppError.RateLimitExceeded(
                            retryAfter = retryAfter,
                        ),
                    )
                }

                // Unauthorized (401)
                HttpStatusCode.Unauthorized -> {
                    Result.failure(
                        AppError.Unauthorized,
                    )
                }

                // Forbidden (403)
                HttpStatusCode.Forbidden -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(
                        AppError.Forbidden(
                            message = errorBody.message,
                        ),
                    )
                }

                // Not Found (404)
                HttpStatusCode.NotFound -> {
                    Result.failure(
                        AppError.NotFound,
                    )
                }

                // Conflict (409)
                HttpStatusCode.Conflict -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(
                        AppError.Conflict(
                            message = errorBody.message ?: "Conflict",
                        ),
                    )
                }

                // Locked (423)
                HttpStatusCode.Locked -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(
                        AppError.AccountLocked(
                            until = errorBody.message ?: "unknown",
                        ),
                    )
                }

                // Validation Error (422)
                HttpStatusCode.UnprocessableEntity -> {
                    val errorBody = response.body<ErrorResponse>()
                    val validationError = parseValidationError(errorBody)
                    Result.failure(validationError)
                }

                // Server Error (500) - retry
                HttpStatusCode.InternalServerError -> {
                    if (attempt < maxRetries - 1) {
                        lastException = Exception("Server error, retrying...")
                        delay(retryDelayMs)
                        return@repeat // продолжаем retry
                    } else {
                        val errorBody = response.body<ErrorResponse>()
                        Result.failure(
                            AppError.NetworkError(
                                code = 500,
                                message = errorBody.message ?: "Internal server error",
                            ),
                        )
                    }
                }

                // Other codes
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(
                        httpCodeToAppError(
                            code = response.status.value,
                            message = errorBody.message ?: "Unknown error",
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            lastException = e
            if (e is kotlinx.serialization.SerializationException) {
                return Result.failure(
                    AppError.UnknownError(
                        throwable = e,
                        message = "Failed to parse response",
                    ),
                )
            }
            // Другие ошибки - retry если это последняя попытка
            if (attempt == maxRetries - 1) {
                return Result.failure(
                    AppError.UnknownError(
                        throwable = e,
                        message = e.message,
                    ),
                )
            }
            delay(retryDelayMs)
        }
    }

    // Если мы здесь, значит все retry были неудачными
    return Result.failure(
        AppError.UnknownError(
            throwable = lastException,
            message = "Failed after $maxRetries retries",
        ),
    )
}

/**
 * ErrorResponse DTO для парсинга ошибок от бэкенда.
 *
 * Бэкенд возвращает ошибки в формате:
 * ```json
 * {
 *   "error": "VALIDATION_ERROR",
 *   "message": "Сообщение на языке пользователя",
 *   "details": {
 *     "errors": [
 *       {"field": "body.email", "message": "field required", "type": "missing"}
 *     ]
 *   },
 *   "error_id": "uuid"
 * }
 * ```
 *
 * @see BACKEND_API_REFERENCE.md секция 6.2 "Error Response Format"
 */
@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val details: ErrorDetails? = null,
    val errorId: String? = null,
)

@Serializable
data class ErrorDetails(
    val errors: List<ValidationErrorItem>? = null,
)

/**
 * Validation Error DTO для детальных ошибок валидации.
 */
@Serializable
data class ValidationErrorItem(
    val field: String? = null,
    val message: String? = null,
    val type: String? = null,
)

/**
 * Парсит Retry-After header из response.
 *
 * @param response HTTP response
 * @return Количество секунд до следующей попытки (дефолт 60)
 */
fun parseRetryAfter(response: HttpResponse): Int {
    val retryAfter = response.headers["Retry-After"]
    return retryAfter?.toIntOrNull() ?: NetworkConstants.DEFAULT_RETRY_AFTER_SECONDS
}

/**
 * Парсит validation error из response body.
 *
 * @param errorBody ErrorResponse body
 * @return ValidationError с детальной информацией
 */
fun parseValidationError(errorBody: ErrorResponse): AppError {
    return try {
        val validationErrors = errorBody.details?.errors
        val firstError = validationErrors?.firstOrNull()
        if (firstError != null) {
            AppError.ValidationError(
                field = firstError.field ?: "unknown",
                message = firstError.message ?: "Validation error",
            )
        } else {
            AppError.ValidationError(
                field = "unknown",
                message = errorBody.message ?: "Validation error",
            )
        }
    } catch (e: Exception) {
        AppError.ValidationError(
            field = "unknown",
            message = errorBody.message ?: "Validation error",
        )
    }
}

/**
 * Расширение для преобразования [Result] с [AppError] в удобный формат.
 *
 * Использование:
 * ```kotlin
 * safeApiCall { ... }.fold(
 *   onSuccess = { data -> ... },
 *   onFailure = { error ->
 *     when (error) {
 *       is AppError.Unauthorized -> ...
 *       is AppError.AccountLocked -> ...
 *       else -> ...
 *     }
 *   }
 * )
 * ```
 */
inline fun <T> Result<T>.foldAppError(
    crossinline onSuccess: (T) -> Unit,
    crossinline onFailure: (AppError) -> Unit,
) {
    fold(
        onSuccess = onSuccess,
        onFailure = { throwable ->
            val error =
                when (throwable) {
                    is AppError -> throwable
                    else -> AppError.UnknownError(throwable)
                }
            onFailure(error)
        },
    )
}

/**
 * Получить [AppError] из [Result] или null если успешен.
 */
val <T> Result<T>.appError: AppError?
    get() =
        exceptionOrNull()?.let { throwable ->
            when (throwable) {
                is AppError -> throwable
                else -> AppError.UnknownError(throwable)
            }
        }
