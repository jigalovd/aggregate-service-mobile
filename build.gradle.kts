plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
}

subprojects {
    group = "com.aggregateservice"
    version = "1.0.0"
}

// Apply Detekt to all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
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
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.startsWith("koverReport") } })
}

// Kover verification task for all modules
tasks.register("koverVerifyAll") {
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name.startsWith("koverVerify") } })
}
