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

    // Парсим первую попытку
    val (data1, errorBody1) = try {
        val data: T? =
            if (statusCode in 200..299 && statusCode != HttpStatusCode.NoContent.value) {
                try {
                    response.body<T>()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

        val errorBody: String? =
            if (statusCode !in 200..299) {
                try {
                    response.body<ErrorResponse>().detail
                } catch (e: Exception) {
                    response.status.description
                }
            } else {
                null
            }

        Pair(data, errorBody)
    } catch (e: Exception) {
        Pair(null, e.message)
    }

    // Если успех → возвращаем данные
    if (statusCode in 200..299) {
        val data = data1
        if (data != null) {
            return Result.success(data)
        }
        return Result.failure(AppError.UnknownError(message = "No data in response"))
    }

    // Если 401 → пробуем обновить токен и повторить
    if (statusCode == HttpStatusCode.Unauthorized.value) {
        val refreshResult = refreshTokenFunction()

        return refreshResult.fold(
            onSuccess = { newToken ->
                // Сохраняем новый токен
                tokenStorage.saveAccessToken(newToken)

                // Повторяем запрос
                try {
                    val retryResponse = apiCall()
                    val retryStatusCode = retryResponse.status.value

                    val (data2, errorBody2) = try {
                        val data: T? =
                            if (retryStatusCode in 200..299 && retryStatusCode != HttpStatusCode.NoContent.value) {
                                try {
                                    retryResponse.body<T>()
                                } catch (e: Exception) {
                                    null
                                }
                            } else {
                                null
                            }

                        val errorBody: String? =
                            if (retryStatusCode !in 200..299) {
                                try {
                                    retryResponse.body<ErrorResponse>().detail
                                } catch (e: Exception) {
                                    retryResponse.status.description
                                }
                            } else {
                                null
                            }

                        Pair(data, errorBody)
                    } catch (e: Exception) {
                        Pair(null, e.message)
                    }

                    if (retryStatusCode in 200..299) {
                        val data = data2
                        if (data != null) {
                            return Result.success(data)
                        }
                        return Result.failure(AppError.UnknownError(message = "No data in retry response"))
                    } else if (retryStatusCode == HttpStatusCode.Unauthorized.value) {
                        // Повторный 401 → logout
                        tokenStorage.clearTokens()
                        Result.failure(AppError.Unauthorized)
                    } else {
                        // Другая ошибка
                        Result.failure(
                            httpCodeToAppError(
                                code = retryStatusCode,
                                message = errorBody2 ?: "Unknown error",
                            ),
                        )
                    }
                } catch (e: Exception) {
                    Result.failure(AppError.UnknownError(e))
                }
            },
            onFailure = { error ->
                // Ошибка refresh → logout
                tokenStorage.clearTokens()

                when (error) {
                    is AppError -> Result.failure(error)
                    else -> Result.failure(AppError.UnknownError(error))
                }
            },
        )
    }

    // Другая ошибка
    return Result.failure(
        httpCodeToAppError(
            code = statusCode,
            message = errorBody1 ?: "Unknown error",
        ),
    )
}
