package com.aggregateservice.core.location

/**
 * Permission status for location access per D-11.
 */
sealed interface LocationPermissionStatus {
    /** Permission granted */
    data object Granted : LocationPermissionStatus
    /** Permission denied (can ask again) */
    data object Denied : LocationPermissionStatus
    /** Permission permanently denied (settings only) */
    data object DeniedPermanently : LocationPermissionStatus
    /** Not yet asked */
    data object Unknown : LocationPermissionStatus
}