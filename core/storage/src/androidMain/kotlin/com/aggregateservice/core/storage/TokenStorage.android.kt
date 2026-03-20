package com.aggregateservice.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Android implementation of [createTokenStorage].
 *
 * Uses DataStore Preferences with Android-specific configuration.
 */

// Extension to create DataStore
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences",
)

/**
 * Create [TokenStorage] for Android platform.
 *
 * **Note:** This function requires Android Context.
 * It's meant to be called from Koin module with androidContext().
 *
 * **Usage in Koin:**
 * ```kotlin
 * single<TokenStorage> { createTokenStorage(androidContext()) }
 * ```
 */
fun createTokenStorage(context: Context): TokenStorage =
    TokenStorageImpl(
        dataStore = context.authDataStore,
    )

