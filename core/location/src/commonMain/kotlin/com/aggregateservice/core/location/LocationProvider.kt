package com.aggregateservice.core.location

/**
 * KMP expect class for device geolocation.
 *
 * Android: FusedLocationProviderClient
 * iOS: CLLocationManager stub (returns Haifa default)
 */
expect class LocationProvider() {

    /**
     * Set the Activity context required for Android location.
     * Must be called before getCurrentLocation() on Android.
     */
    fun setActivity(activity: Any): Unit

    /**
     * Get current device location with specified accuracy.
     *
     * @param accuracy LocationAccuracy level (HIGH, MEDIUM, LOW)
     * @return Result with Location or failure
     */
    suspend fun getCurrentLocation(accuracy: LocationAccuracy): Result<Location>

    /**
     * Request location permission.
     *
     * @return LocationPermissionStatus (Granted, Denied, DeniedPermanently, Unknown)
     */
    suspend fun requestPermission(): LocationPermissionStatus
}