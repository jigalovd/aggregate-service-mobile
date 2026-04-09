package com.aggregateservice.core.logging

import android.content.Context
import co.touchlab.kermit.Logger
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.LogcatWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.io.RollingFileLogWriter
import co.touchlab.kermit.io.RollingFileLogWriterConfig
import kotlinx.io.files.Path

fun initLogging(context: Context, enableLogging: Boolean) {
    val minSeverity = if (enableLogging) Severity.Verbose else Severity.Error
    val writers = mutableListOf<LogWriter>(LogcatWriter())

    if (enableLogging) {
        writers.add(
            RollingFileLogWriter(
                RollingFileLogWriterConfig(
                    logFileName = "aggregate-service",
                    logFilePath = Path(context.filesDir.resolve("logs").path),
                    rollOnSize = 10 * 1024 * 1024,
                    maxLogFiles = 7,
                    logTag = true,
                    prependTimestamp = true,
                ),
            ),
        )
    }

    Logger.setLogWriters(writers)
    Logger.setMinSeverity(minSeverity)
}
