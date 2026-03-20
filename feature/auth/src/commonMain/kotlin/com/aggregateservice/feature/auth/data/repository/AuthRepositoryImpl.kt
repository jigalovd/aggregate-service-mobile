package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.executeWithRefresh
import com.aggregateservice.core.network.httpCodeToAppError
import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.feature.auth.data.dto.AuthResponse
import com.aggregateservice.feature.auth.data.dto.LoginRequest
import com.aggregateservice.feature.auth.data.dto.RefreshTokenResponse
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Реализация репозитория аутентификации (Data слой).
 *
 * **Responsibilities:**
 * - Вызов network API (Ktor)
 * - Маппинг DTO → Domain модели
 * - Управление токенами (через TokenStorage)
 * - Предоставление Flow для AuthState
 *
 * **Architecture:**
 * - Domain слой зависит только от интерфейса AuthRepository
 * - Data слой реализует AuthRepository
 * - Нет прямого доступа к Ktor из Domain или Presentation
 *
 * @property httpClient HTTP клиент для API запросов
 * @property tokenStorage Хранилище токенов
 */
class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    // Private mutable state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)

    // Init: загрузка текущего состояния из TokenStorage
    init {
        // TODO: Подгрузить сохраненный токен при старте
        // Это требует coroutine scope, что не подходит для init блока
        // Лучше делать это в отдельном методе initialize()
    }

    override suspend fun login(credentials: LoginCredentials): Result<AuthState> {
        // 1. Маппим Domain модель → DTO
        val loginRequest = LoginRequest(
            username = credentials.email,
            password = credentials.password,
        )

        // 2. Выполняем API вызов с safe handling
        val response = safeApiCall<AuthResponse> {
            httpClient.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
        }

        // 3. Обрабатываем ответ
        return response.fold(
            onSuccess = { authResponse ->
                val newToken = authResponse.accessToken

                // 4. Сохраняем токен
                tokenStorage.saveAccessToken(newToken)

                // 5. Маппим DTO → Domain модель
                val newState = AuthState.authenticated(
                    token = newToken,
                    email = credentials.email,
                )

                // 6. Обновляем состояние
                _authState.value = newState

                Result.success(newState)
            },
            onFailure = { error ->
                when (error) {
                    is AppError -> Result.failure(error)
                    else -> Result.failure(AppError.UnknownError(error))
                }
            }
        )
    }

    override suspend fun logout() {
        // Очищаем токены
        tokenStorage.clearTokens()

        // Сбрасываем состояние
        _authState.value = AuthState.Initial
    }

    override suspend fun refreshToken(): Result<String> {
        // Refresh token находится в HTTP-only cookie,
        // Ktor автоматически отправит его с запросом
        val response = safeApiCall<RefreshTokenResponse> {
            httpClient.post("auth/refresh") {
                contentType(ContentType.Application.Json)
            }
        }

        return response.fold(
            onSuccess = { refreshResponse ->
                val newToken = refreshResponse.accessToken

                // Сохраняем новый токен
                tokenStorage.saveAccessToken(newToken)

                Result.success(newToken)
            },
            onFailure = { error ->
                when (error) {
                    is AppError -> Result.failure(error)
                    else -> Result.failure(AppError.UnknownError(error))
                }
            }
        )
    }

    override fun observeAuthState(): StateFlow<AuthState> =
        _authState.asStateFlow()

    override fun getCurrentAuthState(): AuthState =
        _authState.value

    /**
     * Выполняет защищенный API вызов с автоматическим обновлением токена.
     *
     * **Использует:** executeWithRefresh для автоматического refresh при 401
     *
     * **Usage Example:**
     * ```kotlin
     * val result = authenticatedApiCall<UserProfile> {
     *     httpClient.get("auth/me") {
     *         withAuth(tokenStorage)
     *     }
     * }
     * ```
     *
     * @param T Тип ответа
     * @param apiCall API вызов для выполнения
     * @return Result с данными или AppError
     */
    private suspend inline fun <reified T : Any> authenticatedApiCall(
        crossinline apiCall: suspend () -> io.ktor.client.statement.HttpResponse,
    ): Result<T> {
        return httpClient.executeWithRefresh(
            tokenStorage = tokenStorage,
            refreshTokenFunction = ::refreshToken,
            apiCall = apiCall
        )
    }
}
