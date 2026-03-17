package com.aggregateservice.core.network

import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.http.*

/**
 * Auth interceptor using Ktor Auth plugin with Bearer tokens.
 * Safely loads tokens asynchronously without blocking the main thread.
 *
 * @param tokenProvider Suspend function that provides the current access token
 */
class AuthInterceptor(
    private val tokenProvider: suspend () -> String?
) {
    /**
     * Installs Bearer authentication on the HttpClient.
     * Uses Ktor's Auth plugin which handles token loading asynchronously.
     */
    fun install(client: HttpClient) {
        client.install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenProvider()
                    if (token != null) {
                        BearerTokens(accessToken = token, refreshToken = "")
                    } else {
                        null
                    }
                }
            }
        }
    }
}
