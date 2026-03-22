package com.aggregateservice.feature.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for profile API response.
 *
 * @property id Unique profile identifier
 * @property userId Associated user identifier
 * @property fullName User's full name
 * @property phone User's phone number
 * @property avatarUrl URL to user's avatar image
 * @property noShowCount Number of missed bookings
 * @property noShowRate Rate of missed bookings
 */
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("full_name")
    val fullName: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("no_show_count")
    val noShowCount: Int = 0,
    @SerialName("no_show_rate")
    val noShowRate: Double = 0.0,
)
