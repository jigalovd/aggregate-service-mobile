package com.aggregateservice.core.navigation

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

/**
 * Navigator for provider-specific screens.
 * All navigation through this navigator will be guarded by role check.
 */
interface ProviderNavigator {
    /**
     * Navigate to provider dashboard.
     * Only works if user has provider role.
     */
    fun navigateToDashboard(navigator: Navigator)

    /**
     * Navigate to provider onboarding (for users who don't have provider role yet).
     */
    fun navigateToOnboarding(navigator: Navigator)

    /**
     * Navigate to provider bookings.
     */
    fun navigateToBookings(navigator: Navigator)

    /**
     * Create dashboard screen.
     */
    fun createDashboardScreen(): Screen

    /**
     * Create onboarding screen.
     */
    fun createOnboardingScreen(): Screen

    /**
     * Create bookings screen.
     */
    fun createBookingsScreen(): Screen
}

/**
 * Guard function that checks if user can navigate to a screen.
 * Returns true if navigation is allowed.
 *
 * @param currentRole Current user role
 * @param requiredRole Role required for the screen
 * @param availableRoles All roles user has
 */
fun canNavigate(
    currentRole: String?,
    requiredRole: RequiredRole,
    availableRoles: List<String>,
): Boolean {
    // Check if user has the required role
    return availableRoles.contains(requiredRole.stringValue)
}

/**
 * Navigate to provider screen if allowed.
 * Does nothing if user doesn't have the required role.
 */
fun navigateToProviderScreen(
    navigator: Navigator,
    currentRole: String?,
    availableRoles: List<String>,
    onAllowed: () -> Unit,
) {
    if (currentRole == RequiredRole.Provider.stringValue ||
        availableRoles.contains(RequiredRole.Provider.stringValue)
    ) {
        onAllowed()
    }
    // If not allowed, silently do nothing (or could show error)
}
