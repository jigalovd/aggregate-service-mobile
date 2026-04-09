package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

object CertificatePins {
    val pins: List<String> =
        listOf(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        )
}

actual val httpClientEngine: HttpClientEngine
    get() =
        OkHttp.create {
            config {
                retryOnConnectionFailure(false)
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(10, TimeUnit.SECONDS)
            }
        }
