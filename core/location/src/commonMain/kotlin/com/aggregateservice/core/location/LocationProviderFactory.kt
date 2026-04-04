package com.aggregateservice.core.location

/**
 * Factory for creating platform-specific LocationProvider.
 */
expect object LocationProviderFactory {
    fun create(): LocationProvider
}
