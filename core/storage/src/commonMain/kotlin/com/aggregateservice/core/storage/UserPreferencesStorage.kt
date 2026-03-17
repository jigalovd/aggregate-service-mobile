package com.aggregateservice.core.storage

interface UserPreferencesStorage {
    suspend fun getUserPreferences(): UserPreferences
    suspend fun updateUserPreferences(preferences: UserPreferences)
    suspend fun setLanguage(language: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setCurrentRole(role: UserRole)
    suspend fun clear()
}
