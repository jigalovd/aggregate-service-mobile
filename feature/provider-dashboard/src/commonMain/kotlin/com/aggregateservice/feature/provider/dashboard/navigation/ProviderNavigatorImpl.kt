package com.aggregateservice.feature.provider.dashboard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.aggregateservice.core.navigation.ProviderNavigator
import com.aggregateservice.feature.provider.bookings.presentation.screen.ProviderBookingsScreen
import com.aggregateservice.feature.provider.dashboard.ProviderDashboardScreen
import com.aggregateservice.feature.provider.onboarding.presentation.screen.ProviderOnboardingScreen

/**
 * Реализация ProviderNavigator для feature:provider-dashboard.
 *
 * Note: createOnboardingScreen() internally calls a @Composable function (ProviderOnboardingScreen).
 * This is handled by wrapping the screen creation in a @Composable context using remember { }.
 * The Navigator.push() call happens inside a @Composable context (e.g., from a Composable screen),
 * so this pattern works correctly. The interface method itself is NOT @Composable to avoid
 * Kotlin/Compose JVM bytecode limitations with @Composable on interface methods.
 */
class ProviderNavigatorImpl : ProviderNavigator {

    override fun createDashboardScreen(): Screen {
        return ProviderDashboardScreen
    }

    /**
     * Creates the onboarding screen.
     * The screen creation wraps the @Composable ProviderOnboardingScreen() call.
     */
    override fun createOnboardingScreen(): Screen {
        return OnboardingScreenWrapper()
    }

    override fun createBookingsScreen(): Screen {
        return ProviderBookingsScreen
    }

    override fun navigateToDashboard(navigator: Navigator) {
        navigator.push(createDashboardScreen())
    }

    override fun navigateToOnboarding(navigator: Navigator) {
        navigator.push(createOnboardingScreen())
    }

    override fun navigateToBookings(navigator: Navigator) {
        navigator.push(createBookingsScreen())
    }
}

/**
 * Wrapper Screen that hosts the @Composable ProviderOnboardingScreen.
 *
 * Since ProviderOnboardingScreen is @Composable, we wrap it in a Screen whose
 * Content composable provides the necessary @Composable context.
 */
private class OnboardingScreenWrapper : Screen {

    @Composable
    override fun Content() {
        // remember {} is used to memoize the screen state
        // but the actual composition happens inside ProviderOnboardingScreen
        ProviderOnboardingScreen()
    }
}
