package com.aggregateservice.core.auth.contract

/**
 * Use case for switching user role.
 * 
 * Delegates to AuthStateMachine which:
 * 1. Updates the AuthState
 * 2. Persists role preference to TokenStore
 * 3. Syncs with backend via PUT /api/v1/users/me/context
 * 
 * @see com.aggregateservice.core.auth.impl.state.AuthStateMachine.switchRole
 */
interface SwitchRoleUseCase {
    /**
     * Switch to a different user role.
     * 
     * @param newRole The role to switch to (e.g., "PROVIDER", "CLIENT")
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(newRole: String): Result<Unit>
}
