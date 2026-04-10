package com.aggregateservice.feature.catalog.domain.repository

import com.aggregateservice.core.common.model.Location

/**
 * Domain interface for persisting last known GPS location.
 * Hides storage implementation (DataStore) behind a clean contract.
 */
interface LocationRepository {
    suspend fun getSavedLocation(): Location?

    suspend fun saveLocation(location: Location)

    suspend fun clearSavedLocation()
}
