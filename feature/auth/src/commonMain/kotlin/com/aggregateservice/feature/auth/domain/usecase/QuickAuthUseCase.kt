package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.firebase.FirebaseAuthApi
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для быстрой аутентификации из диалогов (Domain слой).
 *
 * Объединяет получение Firebase токена и верификацию на бэкенде в один вызов.
 * Используется в [AuthPromptDialog] и других местах, где нужна быстрая
 * аутентификация без перехода на отдельный экран.
 *
 * @property firebaseAuthApi Firebase Authentication API
 * @property repository Репозиторий аутентификации
 */
class QuickAuthUseCase(
    private val firebaseAuthApi: FirebaseAuthApi,
    private val repository: AuthRepository,
) {
    /**
     * Выполняет быструю аутентификацию через Google/Firebase.
     *
     * @return Result с AuthState при успехе, или ошибкой при неудаче
     */
    suspend operator fun invoke(): Result<AuthState> {
        val tokenResult = firebaseAuthApi.signInWithGoogle()
        return tokenResult.fold(
            onSuccess = { token ->
                repository.verifyFirebaseToken(token.authProvider, token.idToken)
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
    }
}
