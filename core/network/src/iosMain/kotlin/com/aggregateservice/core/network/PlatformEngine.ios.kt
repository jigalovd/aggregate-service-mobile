package com.aggregateservice.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual val httpClientEngine: HttpClientEngine
    get() =
        Darwin.create {
            configureSession {
                timeoutIntervalForRequest = 30.0
                timeoutIntervalForResource = 300.0
            }
        }
