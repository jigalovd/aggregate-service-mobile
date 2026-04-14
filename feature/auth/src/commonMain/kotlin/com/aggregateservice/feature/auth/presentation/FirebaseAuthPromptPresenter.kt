package com.aggregateservice.feature.auth.presentation

import com.aggregateservice.core.auth.contract.AuthPromptTrigger
import com.aggregateservice.core.auth.contract.AuthProvider
import com.aggregateservice.core.auth.impl.gate.AuthPromptPresenter
import com.aggregateservice.core.auth.impl.gate.AuthPromptResult
import com.aggregateservice.core.firebase.AuthProviderApi
import com.aggregateservice.core.firebase.PlatformAuthContext

/**
 * MVP implementation of [AuthPromptPresenter] using Firebase Auth.
 *
 * Requires [setContext] to be called from the Compose layer before use.
 * Full implementation will show a bottom sheet / dialog for provider selection.
 */
class FirebaseAuthPromptPresenter(
    private val authProviderApi: AuthProviderApi,
) : AuthPromptPresenter {
    private var context: PlatformAuthContext? = null

    fun setContext(platformContext: PlatformAuthContext) {
        context = platformContext
    }

    override suspend fun prompt(trigger: AuthPromptTrigger): AuthPromptResult? {
        val ctx = context ?: return null
        return try {
            val result = authProviderApi.signInWithGoogle(ctx)
            result.getOrNull()?.let {
                AuthPromptResult(
                    provider = it.provider,
                    idToken = it.idToken,
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}
