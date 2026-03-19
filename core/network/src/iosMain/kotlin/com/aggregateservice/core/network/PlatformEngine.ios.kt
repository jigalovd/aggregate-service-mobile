package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual val httpClientEngine: HttpClientEngine
    get() =
        Darwin.create {
            configureSession {
                timeoutIntervalForRequest = 30.0 // 30 seconds
                timeoutIntervalForResource = 300.0 // 5 minutes
            }
        }
