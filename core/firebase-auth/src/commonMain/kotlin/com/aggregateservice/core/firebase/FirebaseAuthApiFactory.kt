package com.aggregateservice.core.firebase

/**
 * Factory to create platform-specific FirebaseAuthApi instance.
 *
 * Usage:
 * ```kotlin
 * val firebaseAuthApi = FirebaseAuthApiFactory.create()
 * ```
 */
expect object FirebaseAuthApiFactory {
    fun create(): FirebaseAuthApi
}