package com.aggregateservice.feature.auth.data.remote.dto

import com.aggregateservice.feature.auth.domain.model.User
import com.aggregateservice.feature.auth.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val roles: Set<String>,
    val isVerified: Boolean,
    val createdAt: String
) {
    fun toUser(): User = User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        roles = roles.mapNotNull {
            when (it.uppercase()) {
                "CLIENT" -> UserRole.CLIENT
                "PROVIDER" -> UserRole.PROVIDER
                else -> null
            }
        }.toSet(),
        isVerified = isVerified,
        createdAt = createdAt
    )
}
