import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
}

val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

kotlin {
    // Common tests - shared across all platforms
    sourceSets {
        maybeCreate("commonTest").dependencies {
            implementation(kotlin("test"))
            implementation(libs.findLibrary("kotlinx.coroutines.test").get())
            implementation(libs.findLibrary("ktor-client-mock").get())
        }

        // Android unit tests
        maybeCreate("androidUnitTest").dependencies {
            implementation(libs.findLibrary("androidx.test.ext.junit").get())
            implementation(libs.findLibrary("androidx.test.runner").get())
            implementation(libs.findLibrary("robolectric").get())
        }

        // iOS tests (simulator only)
        maybeCreate("iosTest").dependencies {
            // iOS test dependencies if needed
        }
    }
}
