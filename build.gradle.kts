// Load secrets from properties files (for local development)
val secretsFiles = listOf(
    rootProject.file("secrets.properties"),
    rootProject.file("secrets.properties.local"),
    rootProject.file("local.secrets.properties")
)

secretsFiles.forEach { secretsFile ->
    if (secretsFile.exists()) {
        val secrets = java.util.Properties()
        secrets.load(secretsFile.inputStream())

        // Set project properties from secrets
        secrets.forEach keys@{ key, value ->
            if (key !is String) return@keys
            project.extensions.extraProperties.set(key, value)
        }
    }
}

plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
}

subprojects {
    group = "com.aggregateservice"
    version = "1.0.0"
}

// Apply Detekt and Ktlint to all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        ignoreFailures.set(false)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    // Configure Detekt
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files(rootProject.file("config/quality/detekt.yml")))
        buildUponDefaultConfig = true
    }
}

// Apply Kover to all subprojects
subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

// Detekt task for all modules
tasks.register("detektAll") {
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.startsWith("detekt") } })
}

// Ktlint check task for all modules
tasks.register("ktlintCheckAll") {
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.contains("ktlintCheck") } })
}

// Ktlint format task for all modules
tasks.register("ktlintFormatAll") {
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.contains("ktlintFormat") } })
}

// Kover report task for all modules
tasks.register("koverReportAll") {
    group = "verification"
    description = "Generate Kover coverage reports for all modules"
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.startsWith("koverReport") } })
}

// Kover verification task for all modules
tasks.register("koverVerifyAll") {
    group = "verification"
    description = "Verify Kover coverage for all modules"
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.startsWith("koverVerify") } })
}

// Test tasks for all modules
tasks.register("testAll") {
    group = "verification"
    description = "Run all tests across all modules and platforms"
    dependsOn(
        subprojects.map { project -> project.tasks.matching { it.name == "allTests" } }
    )
}

// Test coverage report task
tasks.register("testCoverage") {
    group = "verification"
    description = "Run all tests and generate coverage report"
    dependsOn("testAll", "koverReportAll")
}
