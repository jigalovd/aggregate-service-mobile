package com.aggregateservice.feature.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val roles: Set<UserRole>,
    val isVerified: Boolean,
    val createdAt: String
)

enum class UserRole {
    CLIENT,
    PROVIDER
}
