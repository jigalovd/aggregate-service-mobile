package com.aggregateservice.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*

class AuthInterceptor(
    private val tokenProvider: suspend () -> String?
) {
    fun install(client: HttpClient) {
        client.install(DefaultRequest) {
            val token = kotlinx.coroutines.runBlocking { tokenProvider() }
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}
