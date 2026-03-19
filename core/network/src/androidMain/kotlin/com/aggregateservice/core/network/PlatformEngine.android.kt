package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

actual val httpClientEngine: HttpClientEngine
    get() =
        OkHttp.create {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
        }
