package com.aggregateservice.core.navigation

import androidx.compose.runtime.Composable

/**
 * Guard for role-based access control.
 * Prevents provider actions for client role and vice versa.
 *
 * **Usage:**
 * ```kotlin
 * RoleGuard(
 *     currentRole = authState.currentRole,
 *     requiredRole = RequiredRole.Provider,
 *     onRoleMismatch = { showRoleSwitchDialog() },
 *     content = { ProviderDashboardContent() }
 * )
 * ```
 *
 * **Architecture:**
 * - Composable wrapper for UI role protection
 * - Extension functions for programmatic role checks
 * - Uses sealed class RequiredRole for type safety
 *
 * @param currentRole Current user role
 * @param requiredRole Role required for the protected content
 * @param onRoleMismatch Callback when role doesn't match
 * @param content Protected content to render if role matches
 */
@Suppress("FunctionName")
@Composable
fun RoleGuard(
    currentRole: String?,
    requiredRole: RequiredRole,
    onRoleMismatch: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (currentRole == requiredRole.stringValue) {
        content()
    } else {
        onRoleMismatch()
    }
}

/**
 * Sealed class representing available roles.
 * Provides type-safe role constants for RoleGuard.
 */
sealed class RequiredRole {
    abstract val stringValue: String

    data object Client : RequiredRole() {
        override val stringValue: String = "client"
    }

    data object Provider : RequiredRole() {
        override val stringValue: String = "provider"
    }

    companion object {
        /**
         * Get RequiredRole from string value.
         */
        fun fromString(role: String?): RequiredRole? = when (role) {
            Client.stringValue -> Client
            Provider.stringValue -> Provider
            else -> null
        }

        /**
         * Check if user has the specified role.
         */
        fun canSwitch(role: String, availableRoles: List<String>): Boolean =
            availableRoles.contains(role)
    }
}

/**
 * Extension function for executing role-protected actions.
 *
 * **Usage:**
 * ```kotlin
 * onRoleAction(
 *     currentRole = authState.currentRole,
 *     requiredRole = RequiredRole.Provider,
 *     onRoleMismatch = { showRoleSwitchDialog() },
 *     action = { navigator.push(ProviderDashboard) }
 * )
 * ```
 *
 * @param currentRole Current user role
 * @param requiredRole Role required for the action
 * @param onRoleMismatch Callback when role doesn't match
 * @param action Protected action to execute if role matches
 */
fun onRoleAction(
    currentRole: String?,
    requiredRole: RequiredRole,
    onRoleMismatch: () -> Unit,
    action: () -> Unit,
) {
    if (currentRole == requiredRole.stringValue) {
        action()
    } else {
        onRoleMismatch()
    }
}

/**
 * Extension function to check if user can access provider features.
 */
fun String?.isProvider(): Boolean = this == RequiredRole.Provider.stringValue

/**
 * Extension function to check if user can access client features.
 */
fun String?.isClient(): Boolean = this == RequiredRole.Client.stringValue