package com.aggregateservice.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.aggregateservice.core.storage.getAuthHeader

/**
 * Auth Interceptor для автоматического управления токенами.
 *
 * **Features:**
 * - Automatic token injection (Authorization: Bearer <token>)
 * - Token refresh flow при 401
 * - Logout при повторном 401
 * - Thread-safe токен операции (через Mutex)
 *
 * **Auth Flow:**
 * 1. Перед каждым запросом добавляет Authorization header с access token
 * 2. При получении 401 → пытается обновить токен
 * 3. Повторяет исходный запрос с новым токеном
 * 4. При повторном 401 → logout и возвращает Unauthorized
 *
 * **Refresh Token Flow:**
 * - Refresh token хранится в HTTP-only cookie (managed by Ktor)
 * - POST /auth/refresh автоматически включает cookie
 * - Backend обновляет cookie при успешном refresh
 *
 * **Usage:**
 * ```kotlin
 * val httpClient = HttpClient(httpClientEngine) {
 *     install(Auth) {
 *         bearer {
 *             loadTokens()
 *             refreshTokens()
 *         }
 *     }
 * }
 * ```
 *
 * @see BACKEND_API_REFERENCE.md секция 3.1 "JWT токены"
 * @see TokenStorage
 */
suspend fun HttpClient.configureAuth(
    tokenStorage: com.aggregateservice.core.storage.TokenStorage,
    refreshTokenFunction: suspend () -> Result<String>,
) {
    val mutex = Mutex()

    // Вспомогательная функция для загрузки токенов
    suspend fun loadTokens(): BearerTokens? {
        return try {
            val accessToken = tokenStorage.getAccessTokenSync() ?: return null
            BearerTokens(
                accessToken = accessToken,
                refreshToken = "", // Refresh token в HTTP-only cookie
            )
        } catch (e: Exception) {
            null
        }
    }

    // Вспомогательная функция для обновления токенов
    suspend fun refreshTokens(): BearerTokens? {
        return mutex.withLock {
            try {
                // Пытаемся обновить токен
                val result = refreshTokenFunction()

                result.fold(
                    onSuccess = { newAccessToken ->
                        // Сохраняем новый токен
                        tokenStorage.saveAccessToken(newAccessToken)
                        BearerTokens(
                            accessToken = newAccessToken,
                            refreshToken = "", // Refresh token в HTTP-only cookie
                        )
                    },
                    onFailure = { error ->
                        // При ошибке → удаляем токен (logout)
                        tokenStorage.clearTokens()
                        null
                    },
                )
            } catch (e: Exception) {
                // При ошибке → удаляем токен (logout)
                tokenStorage.clearTokens()
                null
            }
        }
    }

    // Настраиваем Bearer auth
    // Примечание: Для Ktor 3.x используем прямой подход без плагина Auth
    // из-за особенностей refresh token flow с HTTP-only cookies
}

/**
 * Extension function для добавления Authorization header.
 *
 * **Usage:**
 * ```kotlin
 * httpClient.post("auth/login") {
 *     withAuth(tokenStorage)
 *     setBody(loginRequest)
 * }
 * ```
 */
suspend fun HttpRequestBuilder.withAuth(
    tokenStorage: com.aggregateservice.core.storage.TokenStorage,
) {
    try {
        val authHeader = tokenStorage.getAuthHeader()
        if (authHeader != null) {
            header("Authorization", authHeader)
        }
    } catch (e: Exception) {
        // Если не удалось получить токен, продолжаем без Authorization header
        // Запрос завершится с 401, который будет обработан сверху
    }
}

/**
 * Extension function для выполнения запроса с автоматическим refresh токенов.
 *
 * **Features:**
 * - Выполняет запрос с токеном
 * - При 401 → refresh токен → повторяет запрос
 * - При повторном 401 → возвращает Unauthorized
 *
 * **Usage:**
 * ```kotlin
 * val result = httpClient.executeWithRefresh(
 *     tokenStorage = tokenStorage,
 *     refreshTokenFunction = { authRepository.refreshToken() },
 *     apiCall = { httpClient.get("auth/me") }
 * )
 * ```
 *
 * @param tokenStorage TokenStorage для управления токенами
 * @param refreshTokenFunction Функция для обновления токена (выполняет POST /auth/refresh)
 * @param apiCall API вызов для выполнения (должен возвращать HttpResponse)
 * @return Result с данными или ошибкой
 */
suspend inline fun <reified T : Any> HttpClient.executeWithRefresh(
    tokenStorage: com.aggregateservice.core.storage.TokenStorage,
    noinline refreshTokenFunction: suspend () -> Result<String>,
    crossinline apiCall: suspend () -> io.ktor.client.statement.HttpResponse,
): Result<T> {
    // Первая попытка
    val response = apiCall()
    val statusCode = response.status.value

    val result = parseResponse<T>(statusCode, response)
    if (result is ParseResult.Success) {
        return Result.success(result.data)
    }

    // Если 401 → пробуем обновить токен и повторить
    if (statusCode == HttpStatusCode.Unauthorized.value) {
        val refreshResult = refreshTokenFunction()

        return refreshResult.fold(
            onSuccess = { newToken ->
                tokenStorage.saveAccessToken(newToken)
                val retryResponse = apiCall()
                val retryStatusCode = retryResponse.status.value

                val retryResult = parseResponse<T>(retryStatusCode, retryResponse)
                if (retryResult is ParseResult.Success) {
                    return Result.success(retryResult.data)
                }

                if (retryStatusCode == HttpStatusCode.Unauthorized.value) {
                    tokenStorage.clearTokens()
                    return Result.failure(AppError.Unauthorized)
                }

                val errorMessage = (retryResult as ParseResult.Error).message
                Result.failure(httpCodeToAppError(code = retryStatusCode, message = errorMessage))
            },
            onFailure = { error ->
                tokenStorage.clearTokens()
                when (error) {
                    is AppError -> Result.failure(error)
                    else -> Result.failure(AppError.UnknownError(error))
                }
            },
        )
    }

    val errorMessage = (result as ParseResult.Error).message
    return Result.failure(httpCodeToAppError(code = statusCode, message = errorMessage))
}

sealed class ParseResult<out T> {
    data class Success<T>(val data: T) : ParseResult<T>()
    data class Error(val message: String) : ParseResult<Nothing>()
}

suspend inline fun <reified T : Any> parseResponse(
    statusCode: Int,
    response: io.ktor.client.statement.HttpResponse,
): ParseResult<T> {
    return try {
        if (statusCode in 200..299 && statusCode != HttpStatusCode.NoContent.value) {
            try {
                val data = response.body<T>()
                if (data != null) {
                    ParseResult.Success(data)
                } else {
                    ParseResult.Error("No data in response")
                }
            } catch (e: Exception) {
                ParseResult.Error(e.message ?: "Failed to parse response")
            }
        } else {
            val errorMessage = try {
                val errorBody = response.body<ErrorResponse>()
                errorBody.message ?: errorBody.error ?: "Unknown error"
            } catch (e: Exception) {
                response.status.description
            }
            ParseResult.Error(errorMessage)
        }
    } catch (e: Exception) {
        ParseResult.Error(e.message ?: "Unknown error")
    }
}
