package com.aggregateservice.feature.catalog.presentation.cache

import com.aggregateservice.core.common.model.Location

/**
 * In-memory cache for last known GPS location.
 * Presentation concern: survives ScreenModel recreation via Koin singleton.
 */
class LocationCache {
    var lastKnownLocation: Location? = null
        private set

    fun update(location: Location) {
        lastKnownLocation = location
    }

    fun clear() {
        lastKnownLocation = null
    }
}
