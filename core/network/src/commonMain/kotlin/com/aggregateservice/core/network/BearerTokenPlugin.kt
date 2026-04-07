package com.aggregateservice.core.network

import io.ktor.client.plugins.HttpSend
import io.ktor.http.HttpStatusCode
import io.ktor.http.pathSegments
import kotlinx.coroutines.runBlocking

/**
 * Configuration for BearerTokenPlugin.
 *
 * @property tokenHolder TokenHolder for reading access token
 * @property authRefresher AuthRefresher for handling token refresh on 401
 * @property authEventBus AuthEventBus for emitting logout on refresh failure
 * @property excludedPaths Paths where Authorization header is NOT sent
 */
class BearerTokenConfig(
    val tokenHolder: TokenHolder,
    val authRefresher: AuthRefresher,
    val authEventBus: AuthEventBus,
    val excludedPaths: List<String> =
        listOf(
            "api/v1/auth/provider", // login flows
            "api/v1/auth/refresh", // refresh endpoint (cookie sent automatically)
        ),
)

/**
 * Custom Ktor HttpSend interceptor replacing the Ktor Auth plugin.
 *
 * Reads token from TokenHolder on every request (no internal cache).
 * Handles 401 by calling AuthRefresher.refresh().
 * On refresh failure, emits AuthEvent.Logout via AuthEventBus.
 *
 * Excluded paths (no Authorization header):
 * - api/v1/auth/provider - login flows
 * - api/v1/auth/refresh - refresh endpoint (uses HTTP-only cookie)
 *
 * NOT excluded (Authorization header IS sent):
 * - api/v1/auth/logout - backend needs token to blacklist (fixes C2)
 * - api/v1/auth/me - user info
 */
val BearerTokenPlugin =
    createClientPlugin(
        "BearerToken",
        ::BearerTokenConfig,
    ) {
        val tokenHolder = pluginConfig.tokenHolder
        val authRefresher = pluginConfig.authRefresher
        val authEventBus = pluginConfig.authEventBus
        val excludedPaths = pluginConfig.excludedPaths

        install(HttpSend) {
            intercept { context ->
                // Check if this path should be excluded from auth
                val pathSegments = context.url.pathSegments.joinToString("/")
                val isExcluded = excludedPaths.any { pathSegments.startsWith(it) }

                // Read token from TokenHolder on every request (no caching)
                val token = if (!isExcluded) tokenHolder.get() else null

                // Add Authorization header if token exists and path is not excluded
                if (token != null) {
                    context.headers.append("Authorization", "Bearer $token")
                }

                // Execute the request
                val response = execute(context)

                // Handle 401 Unauthorized - attempt token refresh
                if (response.status == HttpStatusCode.Unauthorized && token != null) {
                    // Capture token state before refresh (for concurrent detection)
                    authRefresher.captureTokenState()

                    // Attempt refresh
                    val newToken = runBlocking { authRefresher.refresh() }

                    if (newToken != null) {
                        // Refresh successful - retry with new token
                        context.headers.append("Authorization", "Bearer $newToken")
                        execute(context)
                    } else {
                        // Refresh failed - emit logout event
                        authEventBus.emit(AuthEvent.Logout)
                        response
                    }
                } else {
                    response
                }
            }
        }
    }
