package com.aggregateservice.feature.catalog.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import com.aggregateservice.core.common.model.Location
import com.aggregateservice.core.storage.getFirst
import com.aggregateservice.core.storage.remove
import com.aggregateservice.core.storage.set
import com.aggregateservice.feature.catalog.domain.repository.LocationRepository

/**
 * DataStore-backed implementation of [LocationRepository].
 * Persists last known GPS coordinates for cold start recovery.
 */
class LocationRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : LocationRepository {

    override suspend fun getSavedLocation(): Location? {
        val lat = dataStore.getFirst(KEY_LAT) ?: return null
        val lon = dataStore.getFirst(KEY_LON) ?: return null
        return Location(
            latitude = lat,
            longitude = lon,
            address = "",
            city = "",
            postalCode = null,
            country = null,
        )
    }

    override suspend fun saveLocation(location: Location) {
        dataStore.set(KEY_LAT, location.latitude)
        dataStore.set(KEY_LON, location.longitude)
    }

    override suspend fun clearSavedLocation() {
        dataStore.remove(KEY_LAT)
        dataStore.remove(KEY_LON)
    }

    companion object {
        private val KEY_LAT = doublePreferencesKey("last_latitude")
        private val KEY_LON = doublePreferencesKey("last_longitude")
    }
}
