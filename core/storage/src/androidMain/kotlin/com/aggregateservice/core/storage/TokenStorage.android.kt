package com.aggregateservice.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Android platform-specific DataStore creation.
 */

// Extension to create DataStore
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences",
)

// Extension to create location DataStore
private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "location_preferences",
)

/**
 * Create DataStore for location persistence on Android.
 */
fun createLocationDataStore(context: Context): DataStore<Preferences> = context.locationDataStore

/**
 * Create [TokenStore] for Android platform.
 *
 * Requires Android Context — call from Koin with androidContext().
 */
fun createTokenStore(context: Context): TokenStore =
    TokenStoreImpl(
        dataStore = context.authDataStore,
    )
