package com.aggregateservice.core.storage

/**
 * JVM actual implementation of [createTokenStore].
 *
 * For JVM unit tests, use [FakeTokenStore] directly instead of this function.
 * This implementation exists only to satisfy the expect/actual compiler
 * requirement for the JVM target.
 *
 * **Do not call this in production code.** Use Koin DI to inject TokenStore
 * on Android; for JVM tests use FakeTokenStore.
 */
actual fun createTokenStore(): TokenStore {
    // DataStore on JVM requires file-based protobuf store setup.
    // For unit tests, use FakeTokenStore from core:test-utils instead:
    //   val store: TokenStore = FakeTokenStore()
    throw UnsupportedOperationException(
        "createTokenStore() is not supported on JVM. " +
            "Use FakeTokenStore() for tests.",
    )
}