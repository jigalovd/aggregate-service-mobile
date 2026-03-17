plugins {
    id("kmp-base")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            val compose = project.extensions.getByType<org.jetbrains.compose.ComposeExtension>().dependencies
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}