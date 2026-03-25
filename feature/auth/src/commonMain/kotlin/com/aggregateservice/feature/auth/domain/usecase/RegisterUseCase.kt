package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.network.AppError
import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.model.RegistrationRequest
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для регистрации нового пользователя (Domain слой).
 *
 * **Responsibilities:**
 * - Валидация входных данных (бизнес-правила)
 * - Вызов репозитория для регистрации
 * - Возврат AuthState при успешной регистрации
 *
 * **Important:** UseCase НЕ должен:
 * - Импортировать классы из io.ktor.*
 * - Импортировать Android/iOS классы
 * - Содержать логику UI (Compose код)
 *
 * **Flow:**
 * 1. Валидация email (длина, формат)
 * 2. Валидация password (длина)
 * 3. Валидация roles (минимум одна, валидные значения)
 * 4. Вызов repository.register()
 * 5. Возврат Result с AuthState или AppError
 *
 * @property repository Репозиторий аутентификации
 */
class RegisterUseCase(
    private val repository: AuthRepository,
) {
    /**
     * Выполняет регистрацию нового пользователя.
     *
     * @param request Данные для регистрации
     * @return Result с AuthState при успехе, или AppError при ошибке
     */
    suspend operator fun invoke(request: RegistrationRequest): Result<AuthState> {
        // Бизнес-валидация (дополнительная к валидации в RegistrationRequest)

        // Проверка длины email
        if (request.email.length > 255) {
            return Result.failure(
                AppError.ValidationError(
                    field = "email",
                    message = "Email too long (max 255 characters)",
                ),
            )
        }

        // Проверка формата email (простая проверка)
        if (!EMAIL_PATTERN.matches(request.email)) {
            return Result.failure(
                AppError.ValidationError(
                    field = "email",
                    message = "Invalid email format",
                ),
            )
        }

        // Проверка наличия как минимум одной роли
        if (request.roles.isEmpty()) {
            return Result.failure(
                AppError.ValidationError(
                    field = "roles",
                    message = "At least one role is required",
                ),
            )
        }

        // Вызов репозитория
        return repository.register(request)
    }

    companion object {
        // RFC 5322 simplified pattern
        val EMAIL_PATTERN = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
    }
}
