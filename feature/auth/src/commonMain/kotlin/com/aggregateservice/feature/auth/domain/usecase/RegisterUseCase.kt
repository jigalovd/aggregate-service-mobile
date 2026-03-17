package com.aggregateservice.feature.auth.domain.usecase

import com.aggregateservice.core.utils.Result
import com.aggregateservice.feature.auth.domain.model.User
import com.aggregateservice.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?
    ): Result<User> {
        return authRepository.register(email, password, firstName, lastName)
    }
}
