package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

object CertificatePins {
    val pins: List<String> = listOf(
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    )
}

actual val httpClientEngine: HttpClientEngine
    get() =
        OkHttp.create {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

                certificatePinner(
                    CertificatePinner.Builder()
                        .add("*.aggregateservice.com", *CertificatePins.pins.toTypedArray())
                        .build()
                )
            }
        }
