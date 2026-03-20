package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для выхода пользователя (Domain слой).
 *
 * **Responsibilities:**
 * - Выход из системы
 * - Очистка локальных данных
 *
 * @property repository Репозиторий аутентификации
 */
class LogoutUseCase(
    private val repository: AuthRepository,
) {
    /**
     * Выполняет выход пользователя.
     */
    suspend operator fun invoke() {
        repository.logout()
    }
}
