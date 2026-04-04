package com.aggregateservice.core.location

/**
 * Location accuracy levels per D-10.
 *
 * @property priority Android Priority constant mapping
 */
enum class LocationAccuracy {
    /** GPS, ~10m, high battery consumption */
    HIGH,
    /** Network+GPS, ~100-500m, balanced (DEFAULT) */
    MEDIUM,
    /** Network only, ~1-3km, low battery */
    LOW
}