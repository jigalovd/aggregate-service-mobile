package com.aggregateservice.core.location

/**
 * Location data class for core:location module.
 *
 * This is a separate type from feature:catalog's Location to avoid
 * core module depending on feature module (architecture violation).
 *
 * When CatalogScreenModel integrates with core:location, it will need
 * to map between these types or use a shared model approach.
 *
 * @property latitude Latitude coordinate
 * @property longitude Longitude coordinate
 * @property address Street address (empty string if not available)
 * @property city City name (empty string if not available)
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
) {
    companion object {
        /**
         * Default location (Haifa, Israel).
         * Used as stub for iOS and fallback for Android.
         */
        val DEFAULT = Location(
            latitude = 32.8,
            longitude = 35.0,
            address = "",
            city = "Haifa"
        )
    }
}