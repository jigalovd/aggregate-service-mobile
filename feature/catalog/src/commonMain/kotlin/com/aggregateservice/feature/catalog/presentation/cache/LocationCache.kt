package com.aggregateservice.feature.catalog.presentation.cache

import com.aggregateservice.core.common.model.Location
import com.aggregateservice.feature.catalog.domain.repository.LocationRepository

/**
 * In-memory cache for last known GPS location.
 * Delegates persistence to [LocationRepository] (domain interface).
 * Survives ScreenModel recreation via Koin singleton.
 */
class LocationCache(
    private val locationRepository: LocationRepository,
) {
    var lastKnownLocation: Location? = null
        private set

    /**
     * Restore location from persistent storage.
     * Call before deciding fast/slow path in ScreenModel init.
     */
    suspend fun restore() {
        lastKnownLocation = locationRepository.getSavedLocation()
    }

    /**
     * Update in-memory cache and persist to storage.
     */
    suspend fun update(location: Location) {
        lastKnownLocation = location
        locationRepository.saveLocation(location)
    }

    /**
     * Clear in-memory cache and persistent storage.
     */
    suspend fun clear() {
        lastKnownLocation = null
        locationRepository.clearSavedLocation()
    }
}
