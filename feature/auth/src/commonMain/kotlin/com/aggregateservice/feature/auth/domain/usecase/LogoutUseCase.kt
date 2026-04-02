package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для выхода пользователя (Domain слой).
 *
 * **Responsibilities:**
 * - Выход из системы
 * - Очистка локальных данных
 *
 * @property repository Репозиторий аутентификации
 * @property firebaseAuthApi Firebase Authentication API
 */
class LogoutUseCase(
    private val repository: AuthRepository,
    private val firebaseAuthApi: FirebaseAuthApi,
) {
    /**
     * Выполняет выход пользователя.
     */
    suspend operator fun invoke(): Result<Unit> {
        repository.logout()
        firebaseAuthApi.signOut()
        return Result.success(Unit)
    }
}
