package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для входа через Firebase (Domain слой).
 *
 * **Responsibilities:**
 * - Делегирует sign-in в FirebaseAuthApi
 * - Проверяет Firebase токен через AuthRepository
 * - Возвращает AuthState или ошибку
 *
 * @property firebaseAuthApi Firebase Authentication API
 * @property repository Репозиторий аутентификации
 */
class SignInWithFirebaseUseCase(
    private val firebaseAuthApi: FirebaseAuthApi,
    private val repository: AuthRepository,
) {
    /**
     * Выполняет вход через Firebase.
     *
     * @return Result с AuthState при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(): Result<AuthState> {
        val tokenResult = firebaseAuthApi.signInWithGoogle()
        return tokenResult.fold(
            onSuccess = { token ->
                repository.verifyFirebaseToken(token.authProvider, token.idToken)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}