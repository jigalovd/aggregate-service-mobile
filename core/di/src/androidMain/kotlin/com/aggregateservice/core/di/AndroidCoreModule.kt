package com.aggregateservice.core.di

import com.aggregateservice.core.storage.TokenStorage
import com.aggregateservice.core.storage.createTokenStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val androidCoreModule =
    module {
        single<TokenStorage> { createTokenStorage(androidContext()) }

        single<HttpClientEngine> {
            OkHttp.create {
                config {
                    retryOnConnectionFailure(false)
                    connectTimeout(10, TimeUnit.SECONDS)
                    readTimeout(10, TimeUnit.SECONDS)
                    writeTimeout(10, TimeUnit.SECONDS)
                }
            }
        }
    }
