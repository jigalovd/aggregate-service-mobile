package com.aggregateservice.core.auth.state

sealed interface AuthError {
    data class NetworkError(
        val code: Int,
        val message: String,
    ) : AuthError

    data object NoConnection : AuthError

    data object TokenExpired : AuthError

    data class ServerError(
        val code: Int,
        val message: String,
    ) : AuthError

    data class Unknown(
        val message: String?,
        val cause: Throwable? = null,
    ) : AuthError
}
