package com.aggregateservice.feature.auth

import com.aggregateservice.core.navigation.AuthNavigator
import cafe.adriel.voyager.core.screen.Screen
import com.aggregateservice.feature.auth.presentation.screen.GoogleLoginScreen

/**
 * Implementation of [AuthNavigator] that bridges feature:auth with core:navigation.
 *
 * [createLoginScreen] returns [GoogleLoginScreen] with Google Sign-In.
 * [createRegisterScreen] returns null (registration not yet implemented).
 */
class AuthNavigatorImpl : AuthNavigator {
    override fun createLoginScreen(): Screen = GoogleLoginScreen()

    override fun createRegisterScreen(): Screen? = null
}
