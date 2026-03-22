package com.aggregateservice.feature.profile.domain.model

/**
 * Domain entity representing a user profile.
 *
 * **Note:** Domain models must NOT import Compose/Android dependencies.
 * Stability is ensured by data class immutability.
 *
 * @property id Unique profile identifier
 * @property userId Associated user identifier
 * @property fullName User's full name
 * @property phone User's phone number
 * @property avatarUrl URL to user's avatar image
 * @property noShowCount Number of missed bookings
 * @property noShowRate Rate of missed bookings (0.0 to 1.0)
 */
data class Profile(
    val id: String,
    val userId: String,
    val fullName: String?,
    val phone: String?,
    val avatarUrl: String?,
    val noShowCount: Int,
    val noShowRate: Double,
) {
    /**
     * Display name for the user, falls back to "User" if no name is set.
     */
    val displayName: String
        get() = fullName?.takeIf { it.isNotBlank() } ?: "User"

    /**
     * Whether the user has an avatar.
     */
    val hasAvatar: Boolean
        get() = !avatarUrl.isNullOrBlank()

    /**
     * Whether the user has a good attendance record (no-show rate < 10%).
     */
    val hasGoodAttendance: Boolean
        get() = noShowRate < 0.1
}
