package com.aggregateservice.core.di

import com.aggregateservice.core.config.AppConfig
import com.aggregateservice.core.config.Config
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single<AppConfig> { Config.instance }
}
