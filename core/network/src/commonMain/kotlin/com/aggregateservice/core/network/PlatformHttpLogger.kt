package com.aggregateservice.core.network

import io.ktor.client.plugins.logging.Logger

/**
 * Platform-agnostic HTTP logger wrapper.
 * Actual implementations:
 * - android: delegates to kermit Logger with sanitization
 * - jvm: no-op (suppresses output during tests)
 *
 * Use the [logger] property to get a Ktor [Logger] instance.
 */
expect class PlatformHttpLogger(tag: String) {
    /** Returns a Ktor Logger instance for this platform. */
    val logger: Logger
}