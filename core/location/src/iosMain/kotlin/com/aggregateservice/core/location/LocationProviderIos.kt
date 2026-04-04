package com.aggregateservice.core.location

import com.aggregateservice.feature.catalog.domain.model.Location

/**
 * iOS stub implementation per D-03.
 *
 * Returns Haifa default coordinates (Northern Israel) since real
 * CLLocationManager implementation is deferred to future phase.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENT")
actual class LocationProvider actual constructor() {

    actual fun setActivity(activity: Any) {
        // Stub - no Activity needed for iOS
    }

    actual suspend fun getCurrentLocation(accuracy: LocationAccuracy): Result<Location> {
        // Stub: Returns Haifa coordinates (Northern Israel default)
        return Result.success(
            Location(
                latitude = 32.8,
                longitude = 35.0,
                address = "",
                city = "Haifa"
            )
        )
    }

    actual suspend fun requestPermission(): LocationPermissionStatus {
        // Stub always returns Granted so search works without real location
        return LocationPermissionStatus.Granted
    }
}

actual object LocationProviderFactory {
    actual fun create(): LocationProvider = LocationProvider()
}