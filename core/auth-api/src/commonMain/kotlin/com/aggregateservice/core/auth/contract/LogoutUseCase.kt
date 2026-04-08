package com.aggregateservice.core.auth.contract

interface LogoutUseCase {
    suspend operator fun invoke()
}
