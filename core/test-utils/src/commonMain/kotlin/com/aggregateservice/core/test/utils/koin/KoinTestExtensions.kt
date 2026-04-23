@file:Suppress("unused")

package com.aggregateservice.core.test.utils.koin

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module

/**
 * Test utilities for Koin dependency injection.
 * Simplifies module building and cleanup for tests.
 */

/**
 * Helper to register test modules with automatic cleanup support.
 * Returns an AutoCloseable that will stop Koin when closed.
 *
 * Example:
 * ```
 * val cleanup = testKoinModules(
 *     module { single { MyService() } }
 * )
 * try {
 *     // test code
 * } finally {
 *     cleanup.close()
 * }
 * ```
 *
 * @param modules Variable number of Koin modules to register
 * @return AutoCloseable that will cleanup Koin when closed
 */
fun testKoinModules(vararg modules: Module): AutoCloseable {
    startKoin {
        modules(modules.toList())
    }

    return AutoCloseable {
        stopKoin()
    }
}

/**
 * Helper class for managing a temporary Koin instance in tests.
 * Provides structured lifecycle for test scenarios.
 *
 * Example:
 * ```
 * KoinTestContext()
 *     .registerModules(testModule { single { MyService() } })
 *     .use { context ->
 *         // test code with Koin
 *     }
 * ```
 */
class KoinTestContext : AutoCloseable {
    private var closed = false

    /**
     * Registers modules and returns self for chaining.
     *
     * @param modules Modules to register
     * @return This context for chaining
     */
    fun registerModules(vararg modules: Module): KoinTestContext {
        check(!closed) { "KoinTestContext has been closed" }
        stopKoin()
        startKoin {
            modules(modules.toList())
        }
        return this
    }

    /**
     * Stops Koin and prevents further operations.
     */
    override fun close() {
        if (!closed) {
            stopKoin()
            closed = true
        }
    }
}

/**
 * Creates and starts a KoinTestContext with the given modules.
 * Provides a concise way to set up Koin for a single test.
 *
 * Example:
 * ```
 * startKoinForTest(
 *     module { single { MyService() } }
 * ).use { context ->
 *     // test code
 * }
 * ```
 *
 * @param modules Modules to register
 * @return KoinTestContext that will auto-cleanup when closed
 */
fun startKoinForTest(vararg modules: Module): KoinTestContext {
    return KoinTestContext().registerModules(*modules)
}
