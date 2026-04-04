package com.aggregateservice.core.location

/**
 * Abstraction to hide Activity from commonMain per D-07.
 *
 * Android: AndroidContextProvider(activity)
 * iOS: Not used (stub returns default location)
 */
interface ContextProvider {
    val context: Any
}
