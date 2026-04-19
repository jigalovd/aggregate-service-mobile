package com.aggregateservice.core.location

import com.aggregateservice.core.common.model.Location

/**
 * JVM stub implementation of [LocationProvider].
 * Location features are not supported on JVM — always returns error.
 *
 * For unit tests, inject a mock or test double of LocationProvider.
 */
actual class LocationProvider actual constructor() {
    actual fun setActivity(activity: Any) {
        // no-op on JVM
    }

    actual suspend fun getCurrentLocation(accuracy: LocationAccuracy): Result<Location> =
        Result.failure(
            UnsupportedOperationException(
                "LocationProvider is not supported on JVM. " +
                    "Use a test double in unit tests.",
            ),
        )

    actual suspend fun requestPermission(): LocationPermissionStatus =
        LocationPermissionStatus.Denied
}