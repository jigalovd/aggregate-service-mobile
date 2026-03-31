package com.aggregateservice.feature.auth

import com.aggregateservice.core.navigation.AuthNavigator
import com.aggregateservice.feature.auth.presentation.screen.LoginScreen
import com.aggregateservice.feature.auth.presentation.screen.RegistrationScreen
import cafe.adriel.voyager.core.screen.Screen

/**
 * Implementation of [AuthNavigator] that bridges feature:auth with core:navigation.
 *
 * **Architecture:**
 * - Implements interface from core:navigation
 * - Creates auth screen instances for navigation
 * - Allows feature:favorites and feature:profile to navigate to auth without direct dependency
 *
 * **DI Registration:**
 * ```kotlin
 * single<AuthNavigator> { AuthNavigatorImpl() }
 * ```
 */
class AuthNavigatorImpl : AuthNavigator {
    override fun createLoginScreen(): Screen {
        return LoginScreen()
    }

    override fun createRegisterScreen(): Screen {
        return RegistrationScreen()
    }
}
