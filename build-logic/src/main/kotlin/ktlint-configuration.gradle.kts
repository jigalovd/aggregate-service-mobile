plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

// Configure Ktlint - basic settings only for version 13.0.0
ktlint {
    version.set("13.0.0")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false) // Back to strict mode - we'll handle generated code via .editorconfig
}

// Configure Ktlint tasks
tasks.matching { it.name.contains("ktlint") }.configureEach {
    doFirst {
        println("Running Ktlint on ${project.name}")
    }
}
