package com.aggregateservice.core.logging

import co.touchlab.kermit.Logger
import org.koin.dsl.module

val loggingModule =
    module {
        single<Logger> { Logger }
    }
