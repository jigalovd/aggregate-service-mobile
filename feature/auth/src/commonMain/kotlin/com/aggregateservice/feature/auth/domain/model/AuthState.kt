package com.aggregateservice.feature.auth.domain.model

/**
 * Sealed class representing authentication state.
 *
 * **Architecture:**
 * - Guest: Unregistered user with read-only access (can browse catalog)
 * - Authenticated: Registered user with full access (can book, review)
 *
 * **Guest Mode:**
 * - No token stored
 * - No state persisted
 * - Can browse all screens
 * - Prompted to register on booking/review attempts
 *
 * **Authenticated Mode:**
 * - Token stored securely
 * - Can perform write operations (booking, reviews, favorites)
 */
sealed class AuthState {

    /**
     * User ID for authenticated users, null for guests.
     */
    abstract val userId: String?

    /**
     * Whether user is authenticated (registered).
     */
    abstract val isAuthenticated: Boolean

    /**
     * Whether user can perform write operations (booking, reviews, favorites).
     * Guest = false, Authenticated = true.
     */
    abstract val canWrite: Boolean

    /**
     * Guest state - unregistered user with read-only access.
     *
     * **Characteristics:**
     * - No token stored
     * - No state persisted
     * - Can browse catalog and view provider details
     * - Cannot book, review, or save favorites
     */
    data object Guest : AuthState() {
        override val userId: String? = null
        override val isAuthenticated: Boolean = false
        override val canWrite: Boolean = false
    }

    /**
     * Authenticated state - registered user with full access.
     *
     * @property accessToken JWT access token for API calls
     * @property userId User identifier
     * @property userEmail User email (nullable for session restoration)
     */
    data class Authenticated(
        val accessToken: String,
        override val userId: String,
        val userEmail: String? = null,
    ) : AuthState() {
        override val isAuthenticated: Boolean = true
        override val canWrite: Boolean = true
    }

    companion object {
        /**
         * Initial state for app launch - defaults to Guest.
         * Token restoration will transition to Authenticated if token exists.
         */
        val Initial: AuthState = Guest
    }
}
