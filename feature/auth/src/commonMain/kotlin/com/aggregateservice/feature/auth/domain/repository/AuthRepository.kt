package com.aggregateservice.feature.auth.domain.repository

import com.aggregateservice.feature.auth.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс репозитория аутентификации (Domain слой).
 *
 * **Important:** Это интерфейс из Domain слоя.
 * Реализация находится в data/repository пакете.
 *
 * **Contract:**
 * - logout() очищает токены
 * - observeAuthState() предоставляет Flow для реактивных обновлений
 * - verifyFirebaseToken() выполняет вход через Firebase (Google, Apple, Phone)
 */
interface AuthRepository {
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

    /**
     * Проверяет Firebase токен и выполняет вход через Firebase (Google, Apple, Phone).
     *
     * @param authProvider Провайдер Firebase (google, apple, phone)
     * @param firebaseToken Firebase ID token полученный от Firebase SDK
     * @return Result с AuthState при успехе (включая случаи когда требуется linking),
     *         или AppError при ошибке
     */
    suspend fun verifyFirebaseToken(authProvider: String, firebaseToken: String): Result<AuthState>

    /**
     * Связывает Firebase аккаунт с существующим аккаунтом.
     *
     * Вызывается после verifyFirebaseToken когда returned Result указывает на необходимость linking.
     *
     * @param firebaseToken Firebase token из verify response
     * @param password Пароль существующего аккаунта
     * @return Result с AuthState при успехе, или AppError при ошибке
     */
    suspend fun linkFirebaseAccount(firebaseToken: String, password: String): Result<AuthState>

    /**
     * Инициализирует состояние аутентификации при старте приложения.
     *
     * Проверяет сохранённый токен и валидирует его с бэкендом.
     * Должен вызываться один раз при старте приложения.
     */
    suspend fun initialize()
}
