val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

plugins {
    id("kmp-base")
    id("kmp-android")
    id("testing")
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets {
        maybeCreate("commonMain").dependencies {
            implementation(libs.findLibrary("kotlinx.coroutines.core").get())
        }
    }
}