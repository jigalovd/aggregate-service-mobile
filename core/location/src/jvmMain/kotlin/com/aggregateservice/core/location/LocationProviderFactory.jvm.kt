package com.aggregateservice.core.location

/**
 * JVM stub factory for [LocationProvider].
 * Always returns a stub that throws on use.
 */
actual object LocationProviderFactory {
    actual fun create(): LocationProvider = LocationProvider()
}