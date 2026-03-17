package com.aggregateservice.core.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data class Exception(val exception: kotlin.Exception) : ApiResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isException: Boolean get() = this is Exception

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw kotlin.IllegalStateException("API Error $code: $message")
        is Exception -> throw exception
    }

    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Exception -> this
    }

    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Int, String) -> Unit): ApiResult<T> {
        if (this is Error) action(code, message)
        return this
    }

    inline fun onException(action: (kotlin.Exception) -> Unit): ApiResult<T> {
        if (this is Exception) action(exception)
        return this
    }
}
