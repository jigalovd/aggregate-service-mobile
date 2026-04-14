package com.aggregateservice.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferenceDataStoreFile
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS platform-specific DataStore creation.
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

    return preferenceDataStoreFile(path = preferencesFile.path!!)
}

/**
 * Create [TokenStore] for iOS platform.
 */
actual fun createTokenStore(): TokenStore {
    val dataStore = createIosDataStore()
    return TokenStoreImpl(
        dataStore = dataStore,
    )
}

/**
 * Create DataStore for location persistence on iOS.
 *
 * **File location:** `~/Library/Application Support/location_preferences.preferences_pb`
 */
fun createLocationDataStore(): DataStore<Preferences> {
    val documentDirectory: NSURL = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ),
    ) { "Failed to resolve iOS document directory for location preferences" }

    val preferencesFile =
        documentDirectory
            .URLByAppendingPathComponent("location_preferences")
            .URLByAppendingPathExtension("preferences_pb")

    return preferenceDataStoreFile(
        path = requireNotNull(preferencesFile.path) { "Failed to resolve location preferences file path" },
    )
}
