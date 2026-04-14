package com.aggregateservice.core.storage

/**
 * Android actual implementation of [createTokenStore].
 *
 * **IMPORTANT:** This is a stub that should NOT be used directly.
 * Use Koin injection instead:
 * ```kotlin
 * single<TokenStore> { createTokenStore(androidContext()) }
 * ```
 *
 * This stub exists only to satisfy the expect/actual compiler requirement.
 */
actual fun createTokenStore(): TokenStore {
    throw UnsupportedOperationException(
        "createTokenStore() should not be called directly. " +
            "Use Koin dependency injection instead: " +
            "single<TokenStore> { createTokenStore(androidContext()) }",
    )
}
