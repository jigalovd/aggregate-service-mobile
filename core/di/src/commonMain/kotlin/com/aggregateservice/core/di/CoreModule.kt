package com.aggregateservice.core.di

import org.koin.core.module.Module

expect val platformCoreModule: Module

val coreModule = org.koin.dsl.module {
    // Core dependencies will be added here
}
