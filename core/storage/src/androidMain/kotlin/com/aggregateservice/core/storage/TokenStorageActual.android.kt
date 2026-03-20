package com.aggregateservice.core.storage

/**
 * Android actual implementation of [createTokenStorage].
 *
 * **IMPORTANT:** This is a stub that should NOT be used directly.
 * Use Koin injection instead:
 * ```kotlin
 * single<TokenStorage> { createTokenStorage(androidContext()) }
 * ```
 *
 * This stub exists only to satisfy the expect/actual compiler requirement.
 */
actual fun createTokenStorage(): TokenStorage {
    throw UnsupportedOperationException(
        "createTokenStorage() should not be called directly. " +
        "Use Koin dependency injection instead: " +
        "single<TokenStorage> { createTokenStorage(androidContext()) }"
    )
}
