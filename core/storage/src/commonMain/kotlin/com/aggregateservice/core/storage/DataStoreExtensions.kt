package com.aggregateservice.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Utility extensions for DataStore operations.
 *
 * **Features:**
 * - Generic get/set/delete operations
 * - Synchronous access helpers
 * - Type-safe key management
 */

/**
 * Get a value from DataStore.
 *
 * @param key Preferences key
 * @return Flow emitting the value or null if not found
 */
fun <T> DataStore<Preferences>.get(
    key: Preferences.Key<T>,
): Flow<T?> = data.map { it[key] }

/**
 * Get a value from DataStore synchronously.
 *
 * @param key Preferences key
 * @return Value or null if not found
 */
suspend fun <T> DataStore<Preferences>.getFirst(
    key: Preferences.Key<T>,
): T? = data.first()[key]

/**
 * Save a value to DataStore.
 *
 * @param key Preferences key
 * @param value Value to save
 */
suspend fun <T> DataStore<Preferences>.set(
    key: Preferences.Key<T>,
    value: T,
) {
    edit { preferences ->
        preferences[key] = value
    }
}

/**
 * Delete a value from DataStore.
 *
 * @param key Preferences key
 */
suspend fun <T> DataStore<Preferences>.remove(
    key: Preferences.Key<T>,
) {
    edit { preferences ->
        preferences.remove(key)
    }
}

/**
 * Clear all data from DataStore.
 */
suspend fun DataStore<Preferences>.clear() {
    edit { preferences ->
        preferences.clear()
    }
}

/**
 * Check if DataStore contains a key.
 *
 * @param key Preferences key
 * @return true if key exists
 */
suspend fun <T> DataStore<Preferences>.contains(
    key: Preferences.Key<T>,
): Boolean = data.first().contains(key)

/**
 * Get all data from DataStore.
 *
 * @return Flow emitting all preferences
 */
fun DataStore<Preferences>.getAll(): Flow<Preferences> = data

/**
 * Get all data from DataStore synchronously.
 *
 * @return All preferences
 */
suspend fun DataStore<Preferences>.getAllSync(): Preferences = data.first()
