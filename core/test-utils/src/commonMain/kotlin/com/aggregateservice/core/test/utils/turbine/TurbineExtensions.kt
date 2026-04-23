@file:Suppress("unused")

package com.aggregateservice.core.test.utils.turbine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Test utilities for testing Kotlin Flows.
 * These extensions provide common patterns for flow testing.
 */

/**
 * Collects items from a flow until it completes, returning them as a list.
 *
 * @param T The type of values in the flow
 * @return List of all emitted values
 */
suspend fun <T> Flow<T>.collectAll(): List<T> {
    val items = mutableListOf<T>()
    collect { item ->
        items.add(item)
    }
    return items
}

/**
 * Asserts that a flow emits exactly the expected value first.
 * Returns the actual value for further assertions.
 *
 * @param T The type of values in the flow
 * @param expected The expected first emission
 * @return The actual first value
 * @throws AssertionError if the values don't match
 */
suspend fun <T> Flow<T>.assertFirst(expected: T): T {
    val actual = first()
    return if (actual == expected) {
        actual
    } else {
        throw AssertionError("Expected $expected but got $actual")
    }
}

/**
 * Takes and returns the next N items from a flow.
 *
 * @param T The type of values in the flow
 * @param count Number of items to take
 * @return List of taken items
 */
suspend fun <T> Flow<T>.takeItems(count: Int): List<T> {
    var taken = 0
    val items = mutableListOf<T>()
    collect { item ->
        if (taken < count) {
            items.add(item)
            taken++
        }
        if (taken >= count) {
            return@collect
        }
    }
    return items
}

/**
 * Verifies a flow is complete and emits no values.
 *
 * @param T The type of values in the flow
 * @throws AssertionError if the flow emits any values
 */
suspend fun <T> Flow<T>.assertEmitsNothing() {
    val items = collectAll()
    if (items.isNotEmpty()) {
        throw AssertionError("Expected no emissions but got ${items.size} items: $items")
    }
}

/**
 * Asserts the flow emits a specific count of items.
 *
 * @param T The type of values in the flow
 * @param expectedCount Expected number of items
 * @throws AssertionError if the count doesn't match
 */
suspend fun <T> Flow<T>.assertItemCount(expectedCount: Int) {
    val items = collectAll()
    if (items.size != expectedCount) {
        throw AssertionError("Expected $expectedCount items but got ${items.size}")
    }
}
