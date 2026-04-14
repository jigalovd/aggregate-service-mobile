package com.aggregateservice.core.di

import com.aggregateservice.core.storage.TokenStore
import com.aggregateservice.core.storage.createLocationDataStore
import com.aggregateservice.core.storage.createTokenStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val androidCoreModule =
    module {
        single<TokenStore> { createTokenStore(androidContext()) }

        single<HttpClientEngine> {
            OkHttp.create {
                config {
                    retryOnConnectionFailure(true)
                    connectTimeout(10, TimeUnit.SECONDS)
                    readTimeout(10, TimeUnit.SECONDS)
                    writeTimeout(10, TimeUnit.SECONDS)
                }
            }
        }

        // Location DataStore for persisting last known GPS coordinates
        single<DataStore<Preferences>>(named("location")) {
            createLocationDataStore(androidContext())
        }
    }
