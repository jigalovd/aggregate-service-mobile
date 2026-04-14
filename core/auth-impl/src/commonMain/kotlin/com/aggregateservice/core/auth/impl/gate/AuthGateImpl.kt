package com.aggregateservice.core.auth.impl.gate

import com.aggregateservice.core.auth.contract.AuthGate
import com.aggregateservice.core.auth.contract.AuthPromptTrigger
import com.aggregateservice.core.auth.contract.AuthProvider
import com.aggregateservice.core.auth.contract.AuthStateProvider
import com.aggregateservice.core.auth.contract.SignInUseCase
import com.aggregateservice.core.auth.state.AuthState

class AuthGateImpl(
    private val authStateProvider: AuthStateProvider,
    private val signInUseCase: SignInUseCase,
    private val authPromptPresenter: AuthPromptPresenter,
) : AuthGate {
    override suspend fun <T> run(trigger: AuthPromptTrigger, action: suspend () -> T): Result<T> {
        return when (val state = authStateProvider.authState.value) {
            is AuthState.Authenticated -> runCatching { action() }
            is AuthState.Guest -> promptAndRun(trigger, action)
            is AuthState.Loading -> Result.failure(Exception("Auth state not resolved"))
            is AuthState.Error -> promptAndRun(trigger, action)
        }
    }

    private suspend fun <T> promptAndRun(
        trigger: AuthPromptTrigger,
        action: suspend () -> T,
    ): Result<T> {
        val provider =
            authPromptPresenter.prompt(trigger)
                ?: return Result.failure(Exception("Sign-in cancelled"))

        val signInResult = signInUseCase(provider.provider, provider.idToken)
        return if (signInResult.isSuccess) {
            runCatching { action() }
        } else {
            Result.failure(signInResult.exceptionOrNull() ?: Exception("Sign-in failed"))
        }
    }
}

/**
 * UI contract for presenting the sign-in prompt to the user.
 * Implemented on the presentation layer (Compose).
 */
interface AuthPromptPresenter {
    /**
     * Show sign-in prompt and return the auth result, or null if cancelled.
     */
    suspend fun prompt(trigger: AuthPromptTrigger): AuthPromptResult?
}

data class AuthPromptResult(
    val provider: AuthProvider,
    val idToken: String,
)
