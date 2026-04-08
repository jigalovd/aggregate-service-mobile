package com.aggregateservice.core.auth.contract

interface RefreshTokenUseCase {
    suspend operator fun invoke(): Result<String>
}
