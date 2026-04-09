package com.aggregateservice.core.network

import com.aggregateservice.core.utils.ValidationRule

sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    data class NetworkError(
        val code: Int,
        override val message: String,
        val errorId: String? = null,
    ) : AppError(message)

    data object Unauthorized : AppError("Unauthorized access - token missing or invalid") {
        private fun readResolve(): Any = Unauthorized
    }

    data class Forbidden(
        override val message: String? = null,
    ) : AppError(message)

    data object NotFound : AppError("Resource not found") {
        private fun readResolve(): Any = NotFound
    }

    data class Conflict(
        override val message: String,
        val errorId: String? = null,
    ) : AppError(message)

    data class RateLimitExceeded(
        val retryAfter: Int,
    ) : AppError("Rate limit exceeded. Retry after $retryAfter seconds")

    data class AccountLocked(
        val until: String,
    ) : AppError("Account locked until $until")

    data class FirebaseLinkRequired(
        val firebaseToken: String,
        val email: String,
        val firebaseUid: String,
        val provider: String,
        override val message: String,
    ) : AppError(message)

    data class DomainError(
        val code: String,
        override val message: String,
        val details: Map<String, String> = emptyMap(),
    ) : AppError(message)

    data class ApiValidationError(
        val field: String,
        override val message: String,
        val errors: List<FieldError>? = null,
    ) : AppError(message)

    data class FormValidation(
        val field: String,
        val rule: ValidationRule,
        val parameters: Map<String, Any> = emptyMap(),
    ) : AppError("Validation failed: $field ${rule.name}")

    data class UnknownError(
        val throwable: Throwable? = null,
        override val message: String? = null,
    ) : AppError(message, throwable)
}

data class FieldError(
    val field: String,
    val message: String,
)

fun httpCodeToAppError(
    code: Int,
    message: String = "",
    errorId: String? = null,
): AppError =
    when (code) {
        400 -> AppError.NetworkError(code, message, errorId)
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden(message)
        404 -> AppError.NotFound
        409 -> AppError.Conflict(message, errorId)
        422 -> AppError.ApiValidationError("unknown", message)
        423 -> AppError.AccountLocked(message)
        429 -> AppError.RateLimitExceeded(NetworkConstants.DEFAULT_RETRY_AFTER_SECONDS)
        in 400..499 -> AppError.NetworkError(code, message, errorId)
        in 500..599 -> AppError.NetworkError(code, message, errorId)
        else -> AppError.UnknownError(message = "Unexpected HTTP code: $code")
    }

fun Throwable.toAppError(): AppError =
    when (this) {
        is AppError -> this
        else ->
            AppError.UnknownError(
                throwable = this,
                message = this.message,
            )
    }
