package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.feature.auth.domain.model.AuthState
import com.aggregateservice.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase для наблюдения за состоянием аутентификации (Domain слой).
 *
 * **Responsibilities:**
 * - Предоставление Flow для UI
 * - Реактивные обновления состояния
 *
 * @property repository Репозиторий аутентификации
 */
class ObserveAuthStateUseCase(
    private val repository: AuthRepository,
) {
    /**
     * Возвращает Flow с текущим состоянием аутентификации.
     *
     * @return StateFlow с AuthState
     */
    operator fun invoke(): StateFlow<AuthState> = repository.observeAuthState()
}
