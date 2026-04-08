package com.aggregateservice.core.auth.contract

import com.aggregateservice.core.auth.state.VerifyResult

interface SignInUseCase {
    suspend operator fun invoke(provider: AuthProvider, idToken: String): Result<VerifyResult>
}
