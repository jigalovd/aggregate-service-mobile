package com.aggregateservice.core.network

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * Android implementation of [PlatformHttpLogger].
 * Returns a kermit-backed Ktor Logger with sensitive field sanitization.
 */
actual class PlatformHttpLogger actual constructor(private val tag: String) {
    actual val logger: KtorLogger =
        object : KtorLogger {
            override fun log(message: String) {
                // Sanitize before logging — removes passwords, tokens, Authorization headers
                val sanitized = message.sanitize()
                Logger.withTag(tag).d(sanitized)
            }
        }
}