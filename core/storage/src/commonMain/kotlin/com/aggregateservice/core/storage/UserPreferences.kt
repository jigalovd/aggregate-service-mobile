package com.aggregateservice.core.storage

data class UserPreferences(
    val userId: String?,
    val userRole: UserRole?,
    val language: String,
    val onboardingCompleted: Boolean
)

enum class UserRole {
    CLIENT,
    PROVIDER,
    BOTH
}
