package com.aggregateservice.feature.auth.data.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.core.network.AuthEvent
import com.aggregateservice.core.network.AuthEventBus
import com.aggregateservice.core.network.httpCodeToAppError
import com.aggregateservice.core.network.safeApiCall
import com.aggregateservice.core.storage.TokenHolder
import com.aggregateservice.feature.auth.data.dto.AuthResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseAlreadyLinkedResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseLinkRequest
import com.aggregateservice.feature.auth.data.dto.FirebaseLinkRequiredResponse
import com.aggregateservice.feature.auth.data.dto.FirebaseVerifyRequest
import com.aggregateservice.feature.auth.data.dto.FirebaseVerifyResponse
import com.aggregateservice.feature.auth.data.dto.RefreshTokenResponse
import com.aggregateservice.feature.auth.data.dto.UserResponseDto
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Реализация репозитория аутентификации (Data слой).
 *
 * **Responsibilities:**
 * - Вызов network API (Ktor)
 * - Маппинг DTO → Domain модели
 * - Управление токенами (через TokenHolder)
 * - Предоставление Flow для AuthState
 *
 * **Architecture:**
 * - Domain слой зависит только от интерфейса AuthRepository
 * - Data слой реализует AuthRepository
 * - Нет прямого доступа к Ktor из Domain или Presentation
 *
 * @property httpClient HTTP клиент для API запросов
 * @property tokenHolder Single source of truth for access token
 * @property authEventBus Event bus for logout propagation
 */
class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenHolder: TokenHolder,
    private val authEventBus: AuthEventBus,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)

    @Volatile
    var isInitialized: Boolean = false
        private set

    // Scope for listening to logout events
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        scope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.Logout -> _authState.value = AuthState.Guest
                }
            }
        }
    }

    /**
     * Инициализирует репозиторий, загружая сохраненный токен из TokenHolder.
     *
     * **IMPORTANT:** Этот метод должен быть вызван при старте приложения
     * (например, в Application.onCreate для Android или AppDelegate для iOS).
     *
     * **Why not in init block:**
     * - Требует coroutine scope для async операций
     * - Может блокировать UI если вызывать синхронно
     * - Лучше явно контролировать когда происходит инициализация
     *
     * **Usage:**
     * ```kotlin
     * // In Application class
     * lifecycleScope.launch {
     *     authRepository.initialize()
     * }
     * ```
     */
    override suspend fun initialize() {
        if (isInitialized) {
            return // Already initialized
        }

        // Initialize TokenHolder from persistent storage
        tokenHolder.initFromStorage()
        val token = tokenHolder.get()

        if (token == null) {
            _authState.value = AuthState.Guest
            isInitialized = true
            return
        }

        // GET /me - BearerTokenPlugin handles 401 with auto-refresh
        val userResponse = safeApiCall<UserResponseDto> {
            httpClient.get("/api/v1/auth/me")
        }

        userResponse.fold(
            onSuccess = { user ->
                _authState.value = AuthState.Authenticated(
                    accessToken = token,
                    userId = user.id,
                    userEmail = user.email,
                )
            },
            onFailure = { error ->
                // If 401, BearerTokenPlugin would have already tried refresh.
                // If we get here, both tokens are invalid.
                tokenHolder.clear()
                _authState.value = AuthState.Guest
            }
        )
        isInitialized = true
    }

    override suspend fun logout() {
        // POST /auth/logout - BearerTokenPlugin adds Authorization header (not excluded)
        safeApiCall<Unit> { httpClient.post("/api/v1/auth/logout") }
        tokenHolder.clear()
        _authState.value = AuthState.Guest
    }

    override suspend fun refreshToken(): Result<String> {
        // Refresh token находится в HTTP-only cookie,
        // Ktor автоматически отправит его с запросом
        val response = safeApiCall<RefreshTokenResponse> {
            httpClient.post("/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
            }
        }

        return response.fold(
            onSuccess = { refreshResponse ->
                val newToken = refreshResponse.accessToken

                // Store new token via TokenHolder (persists to DataStore)
                tokenHolder.set(newToken)

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

    override suspend fun verifyFirebaseToken(
        authProvider: String,
        firebaseToken: String,
    ): Result<AuthState> {
        val verifyRequest = FirebaseVerifyRequest(
            firebaseToken = firebaseToken,
        )

        val response = safeApiCall<FirebaseVerifyResponse> {
            httpClient.post("/api/v1/auth/provider/verify") {
                contentType(ContentType.Application.Json)
                setBody(verifyRequest)
            }
        }

        // 3. Обрабатываем ответ
        return response.fold(
            onSuccess = { firebaseResponse ->
                when (firebaseResponse) {
                    is FirebaseAlreadyLinkedResponse -> {
                        // Firebase аккаунт уже связан - получаем access token
                        val newToken = firebaseResponse.accessToken

                        // 4. Сохраняем токен via TokenHolder
                        tokenHolder.set(newToken)

                        // 5. Маппим в AuthState.Authenticated
                        val newState = AuthState.Authenticated(
                            accessToken = newToken,
                            userId = firebaseResponse.user.id,
                            userEmail = firebaseResponse.user.email,
                        )

                        _authState.value = newState

                        Result.success(newState)
                    }

                    is FirebaseLinkRequiredResponse -> {
                        Result.failure(
                            AppError.FirebaseLinkRequired(
                                firebaseToken = firebaseToken,
                                email = firebaseResponse.email,
                                firebaseUid = firebaseResponse.firebaseUid,
                                provider = firebaseResponse.provider,
                                message = firebaseResponse.message ?: "Account linking required",
                            ),
                        )
                    }
                }
            },
            onFailure = { error ->
                when (error) {
                    is AppError -> Result.failure(error)
                    else -> Result.failure(AppError.UnknownError(error))
                }
            }
        )
    }

    override suspend fun linkFirebaseAccount(
        firebaseToken: String,
        password: String,
    ): Result<AuthState> {
        // 1. Маппим в DTO
        val linkRequest = FirebaseLinkRequest(
            firebaseToken = firebaseToken,
            password = password,
        )

        // 2. Выполняем API вызов
        val response = safeApiCall<AuthResponse> {
            httpClient.post("/api/v1/auth/provider/link") {
                contentType(ContentType.Application.Json)
                setBody(linkRequest)
            }
        }

        // 3. Обрабатываем ответ
        return response.fold(
            onSuccess = { authResponse ->
                val newToken = authResponse.accessToken

                // 4. Сохраняем токен via TokenHolder
                tokenHolder.set(newToken)

                // 5. Маппим в AuthState
                val newState = AuthState.Authenticated(
                    accessToken = newToken,
                    userId = authResponse.user.id,
                    userEmail = authResponse.user.email,
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
}
