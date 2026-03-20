package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.LoginCredentials
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для входа пользователя (Domain слой).
 *
 * **Responsibilities:**
 * - Валидация входных данных (бизнес-правила)
 * - Вызов репозитория
 * - Преобразование ошибок в UI-формат (если нужно)
 *
 * **Important:** UseCase НЕ должен:
 * - Импортировать классы из io.ktor.*
 * - Импортировать Android/iOS классы
 * - Содержать логику UI (Compose код)
 *
 * @property repository Репозиторий аутентификации
 */
class LoginUseCase(
    private val repository: AuthRepository,
) {
    /**
     * Выполняет вход пользователя.
     *
     * @param credentials Credential'ы пользователя
     * @return Result с AuthState при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(credentials: LoginCredentials): Result<AuthState> {
        // Бизнес-валидация (дополнительная к валидации в LoginCredentials)
        if (credentials.email.length > 255) {
            return Result.failure(
                AppError.ValidationError(
                    field = "email",
                    message = "Email too long (max 255 characters)",
                ),
            )
        }

        // Вызов репозитория
        return repository.login(credentials)
    }
}
