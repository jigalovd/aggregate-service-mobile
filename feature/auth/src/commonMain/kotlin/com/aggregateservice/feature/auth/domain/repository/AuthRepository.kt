package com.aggregateservice.feature.auth.domain.repository

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс репозитория аутентификации (Domain слой).
 *
 * **Important:** Это интерфейс из Domain слоя.
 * Реализация находится в data/repository пакете.
 *
 * **Contract:**
 * - login() возвращает Result с AuthState или AppError
 * - register() возвращает Result с AuthState или AppError
 * - logout() очищает токены
 * - observeAuthState() предоставляет Flow для реактивных обновлений
 */
interface AuthRepository {
    /**
     * Выполняет вход пользователя.
     *
     * @param credentials Credential'ы пользователя
     * @return Result с AuthState при успехе, или AppError при ошибке
     */
    suspend fun login(credentials: LoginCredentials): Result<AuthState>

    /**
     * Выполняет регистрацию нового пользователя.
     *
     * @param request Данные для регистрации
     * @return Result с AuthState при успехе, или AppError при ошибке
     */
    suspend fun register(request: RegistrationRequest): Result<AuthState>

    /**
     * Выполняет выход пользователя (очищает токены).
     */
    suspend fun logout()

    /**
     * Обновляет access token используя refresh token (HTTP-only cookie).
     *
     * @return Result с новым access token, или AppError
     */
    suspend fun refreshToken(): Result<String>

    /**
     * Наблюдает за состоянием аутентификации.
     *
     * @return StateFlow с текущим AuthState
     */
    fun observeAuthState(): StateFlow<AuthState>

    /**
     * Получает текущее состояние аутентификации синхронно.
     *
     * @return Текущий AuthState
     */
    fun getCurrentAuthState(): AuthState
}
