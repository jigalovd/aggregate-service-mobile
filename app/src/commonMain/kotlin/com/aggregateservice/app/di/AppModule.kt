package com.aggregateservice.app.di

import com.aggregateservice.feature.auth.di.authModule
import org.koin.dsl.module

val appModule = module {
    includes(authModule)
}
