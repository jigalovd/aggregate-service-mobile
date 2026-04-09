package com.aggregateservice.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend inline fun <reified T : Any> safeApiCall(
    maxRetries: Int = NetworkConstants.MAX_RETRIES,
    retryDelayMs: Long = NetworkConstants.RETRY_DELAY_MS,
    crossinline apiCall: suspend () -> HttpResponse,
): Result<T> {
    var lastException: Throwable? = null

    repeat(maxRetries) { attempt ->
        try {
            val response = apiCall()

            return when (response.status) {
                HttpStatusCode.OK,
                HttpStatusCode.Created,
                HttpStatusCode.Accepted,
                -> {
                    val data = response.body<T>()
                    Result.success(data)
                }

                HttpStatusCode.NoContent -> {
                    @Suppress("UNCHECKED_CAST")
                    val result =
                        if (null is T) {
                            Result.success(null as T)
                        } else {
                            Result.success(Unit as T)
                        }
                    result
                }

                HttpStatusCode.TooManyRequests -> {
                    val retryAfter = parseRetryAfter(response)
                    Result.failure(
                        AppError.RateLimitExceeded(
                            retryAfter = retryAfter,
                        ),
                    )
                }

                HttpStatusCode.Unauthorized -> {
                    Result.failure(AppError.Unauthorized)
                }

                HttpStatusCode.Forbidden -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(
                        AppError.Forbidden(
                            message = errorBody.message,
                        ),
                    )
                }

                HttpStatusCode.NotFound -> {
                    Result.failure(AppError.NotFound)
                }

                HttpStatusCode.Conflict -> {
                    val body = response.bodyAsText()
                    val appError = try {
                        val errorResponse = Json.decodeFromString<ErrorResponse>(body)
                        if (errorResponse.error == "FIREBASE_LINK_REQUIRED") {
                            AppError.FirebaseLinkRequired(
                                firebaseToken = errorResponse.details?.errors?.firstOrNull()?.message ?: "",
                                email = errorResponse.message ?: "",
                                firebaseUid = "",
                                provider = "",
                                message = errorResponse.message ?: "Firebase link required",
                            )
                        } else {
                            AppError.Conflict(
                                message = errorResponse.message ?: "Conflict",
                                errorId = errorResponse.errorId,
                            )
                        }
                    } catch (_: Exception) {
                        try {
                            val detailResponse = Json.decodeFromString<DetailErrorResponse>(body)
                            AppError.Conflict(message = detailResponse.detail, errorId = null)
                        } catch (_: Exception) {
                            AppError.Conflict(message = body, errorId = null)
                        }
                    }
                    Result.failure(appError)
                }

                HttpStatusCode.Locked -> {
                    val errorBody = response.body<ErrorResponse>()
                    val lockUntil = errorBody.details?.errors?.firstOrNull()?.message
                        ?: errorBody.message ?: "unknown"
                    Result.failure(AppError.AccountLocked(until = lockUntil))
                }

                HttpStatusCode.UnprocessableEntity -> {
                    val body = response.bodyAsText()
                    val errorResponse = try {
                        Json.decodeFromString<ErrorResponse>(body)
                    } catch (_: Exception) {
                        return Result.failure(
                            AppError.ApiValidationError(
                                field = "unknown",
                                message = body,
                            ),
                        )
                    }

                    if (errorResponse.error != null) {
                        Result.failure(
                            AppError.DomainError(
                                code = errorResponse.error,
                                message = errorResponse.message ?: "Error",
                                details = errorResponse.details?.toFlatMap() ?: emptyMap(),
                            ),
                        )
                    } else if (errorResponse.details?.errors?.isNotEmpty() == true) {
                        Result.failure(parseValidationError(errorResponse))
                    } else {
                        Result.failure(
                            AppError.ApiValidationError(
                                field = "unknown",
                                message = errorResponse.message ?: "Validation error",
                            ),
                        )
                    }
                }

                HttpStatusCode.InternalServerError -> {
                    if (attempt < maxRetries - 1) {
                        lastException = Exception("Server error, retrying...")
                        delay(retryDelayMs)
                        return@repeat
                    } else {
                        val errorBody = response.body<ErrorResponse>()
                        Result.failure(
                            AppError.NetworkError(
                                code = 500,
                                message = errorBody.message ?: "Internal server error",
                            ),
                        )
                    }
                }

                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(
                        httpCodeToAppError(
                            code = response.status.value,
                            message = errorBody.message ?: "Unknown error",
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            lastException = e
            if (e is kotlinx.serialization.SerializationException) {
                return Result.failure(
                    AppError.UnknownError(
                        throwable = e,
                        message = "Failed to parse response",
                    ),
                )
            }
            if (attempt == maxRetries - 1) {
                return Result.failure(
                    AppError.UnknownError(
                        throwable = e,
                        message = e.message,
                    ),
                )
            }
            delay(retryDelayMs)
        }
    }

    return Result.failure(
        AppError.UnknownError(
            throwable = lastException,
            message = "Failed after $maxRetries retries",
        ),
    )
}

@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val details: ErrorDetails? = null,
    @SerialName("error_id")
    val errorId: String? = null,
)

@Serializable
data class DetailErrorResponse(val detail: String)

@Serializable
data class ErrorDetails(
    val errors: List<ValidationErrorItem>? = null,
)

@Serializable
data class ValidationErrorItem(
    val field: String? = null,
    val message: String? = null,
    val type: String? = null,
)

fun ErrorDetails.toFlatMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    errors?.forEach { item ->
        if (item.field != null && item.message != null) {
            map[item.field] = item.message
        }
    }
    return map
}

fun parseRetryAfter(response: HttpResponse): Int {
    val retryAfter = response.headers["Retry-After"]
    return retryAfter?.toIntOrNull() ?: NetworkConstants.DEFAULT_RETRY_AFTER_SECONDS
}

fun parseValidationError(errorBody: ErrorResponse): AppError {
    return try {
        val validationErrors = errorBody.details?.errors
        val firstError = validationErrors?.firstOrNull()
        if (firstError != null) {
            AppError.ApiValidationError(
                field = firstError.field ?: "unknown",
                message = firstError.message ?: "Validation error",
            )
        } else {
            AppError.ApiValidationError(
                field = "unknown",
                message = errorBody.message ?: "Validation error",
            )
        }
    } catch (e: Exception) {
        AppError.ApiValidationError(
            field = "unknown",
            message = errorBody.message ?: "Validation error",
        )
    }
}
