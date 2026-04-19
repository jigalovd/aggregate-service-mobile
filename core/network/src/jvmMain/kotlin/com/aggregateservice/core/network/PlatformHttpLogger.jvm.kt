package com.aggregateservice.core.network

import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * JVM implementation of [PlatformHttpLogger].
 * Returns a no-op Ktor Logger that discards all output during tests.
 */
actual class PlatformHttpLogger actual constructor(tag: String) {
    actual val logger: KtorLogger =
        object : KtorLogger {
            override fun log(message: String) {
                // no-op for JVM tests
            }
        }
}