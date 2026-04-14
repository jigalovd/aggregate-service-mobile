package com.aggregateservice.core.auth.impl.usecase

import com.aggregateservice.core.auth.contract.AuthProvider
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.auth.impl.repository.AuthRepository
import com.aggregateservice.core.auth.impl.state.AuthStateMachine
import com.aggregateservice.core.auth.state.VerifyResult

class SignInUseCaseImpl(
    private val repository: AuthRepository,
    private val authStateMachine: AuthStateMachine,
) : SignInUseCase {
    override suspend fun invoke(provider: AuthProvider, idToken: String): Result<VerifyResult> {
        val result = repository.verifyFirebaseToken(provider.name.lowercase(), idToken)
        return result.fold(
            onSuccess = { verifyResult ->
                val authenticated = verifyResult as VerifyResult.Authenticated
                authStateMachine.signIn(
                    userId = authenticated.userId,
                    email = authenticated.email,
                    roles = authenticated.roles,
                    currentRole = authenticated.currentRole,
                )
                Result.success(verifyResult)
            },
            onFailure = { Result.failure(it) },
        )
    }
}
