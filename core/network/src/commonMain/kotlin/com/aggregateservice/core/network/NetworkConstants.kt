package com.aggregateservice.core.network

object NetworkConstants {
    const val MAX_RETRIES = 1  // 1 initial attempt + 1 retry = max 2 attempts
    const val RETRY_DELAY_MS = 1000L
    const val DEFAULT_RETRY_AFTER_SECONDS = 60
    const val TIMEOUT_SECONDS = 30L
    const val TIMEOUT_MS = 30_000L
    const val RESOURCE_TIMEOUT_SECONDS = 300.0
}
