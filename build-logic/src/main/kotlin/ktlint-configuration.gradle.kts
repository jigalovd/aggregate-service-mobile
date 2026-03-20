val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

// Configure Ktlint
ktlint {
    version.set(libs.findVersion("ktlint").get().requiredVersion)
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(true)

    // Exclude generated code from ktlint checks
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// Configure Ktlint tasks
tasks.matching { it.name.contains("ktlint") }.configureEach {
    doFirst {
        println("Running Ktlint on ${project.name}")
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    exclude { it.file.path.replace('\\', '/').contains("/build/") }
}

tasks.matching { it.name.matches(Regex("ktlint.+SourceSetCheck")) }.configureEach {
    enabled = false
}

tasks.matching { it.name.matches(Regex("runKtlintCheckOver.+SourceSet")) }.configureEach {
    enabled = false
}
