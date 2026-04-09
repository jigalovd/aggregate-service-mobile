package com.aggregateservice.core.auth.impl.network

import com.aggregateservice.core.auth.state.AuthError
import com.aggregateservice.core.network.AppError

fun AppError.toAuthError(): AuthError =
    when (this) {
        is AppError.NetworkError -> AuthError.NetworkError(code, message)
        is AppError.Unauthorized -> AuthError.TokenExpired
        is AppError.Forbidden -> AuthError.NetworkError(403, message ?: "Forbidden")
        is AppError.NotFound -> AuthError.NetworkError(404, "Resource not found")
        is AppError.AccountLocked -> AuthError.NetworkError(423, "Account locked until $until")
        is AppError.ValidationError -> AuthError.NetworkError(422, "$field: $message")
        is AppError.SlotNotAvailable -> AuthError.NetworkError(409, reason)
        is AppError.RateLimitExceeded -> AuthError.NetworkError(429, "Rate limit exceeded")
        is AppError.Conflict -> AuthError.NetworkError(409, message)
        is AppError.UnknownError -> AuthError.Unknown(message, throwable)
        is AppError.FirebaseLinkRequired -> AuthError.NetworkError(409, message)
    }
