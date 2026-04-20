package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.SwitchRoleUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.state.AuthStateMachine

/**
 * Implementation of SwitchRoleUseCase.
 * 
 * Wraps AuthStateMachine.switchRole() which handles:
 * 1. Local state update for responsive UI
 * 2. TokenStore persistence for app restart
 * 3. Backend API call (PUT /api/v1/users/me/context)
 * 
 * @param authStateMachine State machine for auth operations
 * @param repository Repository for backend API calls
 */
class SwitchRoleUseCaseImpl(
    private val authStateMachine: AuthStateMachine,
    private val repository: AuthRepository,
) : SwitchRoleUseCase {
    
    override suspend fun invoke(newRole: String): Result<Unit> {
        return runCatching {
            authStateMachine.switchRole(newRole)
        }
    }
}
