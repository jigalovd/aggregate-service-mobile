package com.aggregateservice.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferenceDataStoreFile
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of [createTokenStorage].
 *
 * Uses DataStore Preferences with iOS-specific file path.
 *
 * **Note:** Refresh token is stored in HTTP-only cookie by URLSession.
 * We only manage the access token on iOS.
 */

/**
 * Create DataStore for iOS platform.
 *
 * **File location:** `~/Library/Application Support/auth_preferences.preferences_pb`
 */
private fun createIosDataStore(): DataStore<Preferences> {
    val documentDirectory: NSURL =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )

    val preferencesFile =
        documentDirectory
            .URLByAppendingPathComponent("auth_preferences")
            .URLByAppendingPathExtension("preferences_pb")

    // Create DataStore with the produced file
    return preferenceDataStoreFile(path = preferencesFile.path!!)
}

/**
 * Create [TokenStorage] for iOS platform.
 *
 * **Note:** iOS DataStore requires additional setup.
 * This implementation uses the iOS-specific file path.
 */
actual fun createTokenStorage(): TokenStorage {
    val dataStore = createIosDataStore()
    return TokenStorageImpl(
        dataStore = dataStore,
    )
}

