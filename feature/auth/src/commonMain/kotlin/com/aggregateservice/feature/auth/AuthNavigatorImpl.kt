package com.aggregateservice.feature.auth

import com.aggregateservice.core.navigation.AuthNavigator
import cafe.adriel.voyager.core.screen.Screen

/**
 * Implementation of [AuthNavigator] that bridges feature:auth with core:navigation.
 *
 * **Note:** Firebase Auth handles authentication via its own UI.
 * Auth is triggered via [com.aggregateservice.feature.auth.presentation.component.AuthPromptDialog]
 * with Firebase Auth callbacks, not via screen navigation.
 *
 * [createLoginScreen] and [createRegisterScreen] return null because auth flow
 * is handled via AuthPromptDialog callbacks that call FirebaseAuthApi.signInWithGoogle()
 * and then AuthRepository.verifyFirebaseToken().
 */
class AuthNavigatorImpl : AuthNavigator {
    /**
     * Returns null because auth is handled via AuthPromptDialog with Firebase Auth callbacks.
     */
    override fun createLoginScreen(): Screen? = null

    /**
     * Returns null because auth is handled via AuthPromptDialog with Firebase Auth callbacks.
     */
    override fun createRegisterScreen(): Screen? = null
}
