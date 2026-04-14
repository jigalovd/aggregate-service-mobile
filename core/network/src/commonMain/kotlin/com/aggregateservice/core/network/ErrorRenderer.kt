package com.aggregateservice.core.network

import com.aggregateservice.core.i18n.I18nProvider
import com.aggregateservice.core.i18n.StringKey

fun AppError.toUserMessage(i18n: I18nProvider): String =
    when (this) {
        is AppError.FormValidation -> {
            val key = "validation.${rule.name.lowercase()}"
            val templateArgs = parameters.values.toTypedArray()
            val templateMessage = i18n.get(key, *templateArgs)
            "$field: $templateMessage"
        }

        is AppError.NetworkError -> message
        is AppError.Unauthorized -> i18n[StringKey.Error.UNAUTHORIZED]
        is AppError.Forbidden -> message ?: i18n[StringKey.Error.SERVER]
        is AppError.NotFound -> i18n[StringKey.Error.NETWORK]
        is AppError.Conflict -> message
        is AppError.RateLimitExceeded -> i18n.get("error_rate_limit", retryAfter.toString())
        is AppError.AccountLocked -> i18n.get("error_account_locked", until)
        is AppError.FirebaseLinkRequired -> message
        is AppError.DomainError -> message
        is AppError.ApiValidationError -> message ?: i18n[StringKey.Error.VALIDATION]
        is AppError.UnknownError -> message ?: i18n[StringKey.Error.UNKNOWN]
    }
