package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.feature.auth.domain.repository.AuthRepository

/**
 * UseCase для инициализации аутентификации при старте приложения (Domain слой).
 *
 * **Responsibilities:**
 * - Проверяет сохранённый токен и валидирует его с бэкендом
 * - Обеспечивает "тихую" повторную аутентификацию без UI
 *
 * @property repository Репозиторий аутентификации
 */
class InitializeAuthUseCase(
    private val repository: AuthRepository,
) {
    /**
     * Инициализирует состояние аутентификации.
     *
     * Должен вызываться один раз при старте приложения.
     * Если пользователь уже входил и токен валиден - устанавливает Authenticated.
     * Если токен невалиден или отсутствует - оставляет Guest.
     */
    suspend operator fun invoke() {
        repository.initialize()
    }
}
